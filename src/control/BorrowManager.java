/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */
import entitiy.Book;
import entitiy.BorrowRecord;
import entitiy.Student;
import adt.BPlusTree;
import adt.BPlusTree.SimpleList;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import utility.IndexHelper;

public class BorrowManager {

    private final BPlusTree<String, BorrowRecord> mainTree;
    private final BPlusTree<String, Student> studentTree;
    private final BPlusTree<String, Book> bookTree;

    private BPlusTree<String, SimpleList<String>> studentIndex;
    private BPlusTree<String, SimpleList<String>> statusIndex;

    public BorrowManager(BPlusTree<String, Book> sharedBookTree, BPlusTree<String, Student> studentTree) {
        this.bookTree = sharedBookTree;
        this.studentTree = studentTree;

        String path = "BorrowRecords.bin";
        BPlusTree<String, BorrowRecord> loaded = BPlusTree.load(path);
        if (loaded != null) {
            this.mainTree = loaded;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        rebuildAllIndex();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Auto-Save] Saving borrow record data to disk...");
            if (this.mainTree != null) {
                this.mainTree.commit();
            }
        }));
    }

    public boolean borrowBook(String studentId, String bookId) {

        if (getActiveBorrowCount(studentId) >= 3) {
            System.out.println("Limit reached: Student already has 3 books on loan.");
            return false;
        }

        Book book = bookTree.read(bookId);
        if (book == null || !"Available".equalsIgnoreCase(book.getAvailability())) {
            return false;
        }
        Student s = studentTree.read(studentId);
        if (s == null) {
            JOptionPane.showMessageDialog(null, "Student not found!");
            return false;
        }

        book.setAvailability("Borrowed by " + studentId);
        bookTree.update(bookId, book);

        String txId = "TX" + System.currentTimeMillis();
        BorrowRecord record = new BorrowRecord(
                txId, bookId, book.getTitle(),
                studentId, s.getName(),
                LocalDate.now().toString(),
                LocalDate.now().plusDays(14).toString(),
                "On Loan"
        );

        mainTree.create(txId, record);
        IndexHelper.addToIndex(statusIndex, record.getStatus(), record.getTransactionId());
        IndexHelper.addToIndex(studentIndex, record.getStudentId(), record.getTransactionId());
        return true;
    }

    public int getActiveBorrowCount(String studentId) {
        int count = 0;
        SimpleList<String> txIds = studentIndex.read(studentId);
        if (txIds == null) {
            return 0;
        }
        for (int i = 0; i < txIds.size(); i++) {
            String txId = txIds.get(i);
            BorrowRecord r = mainTree.read(txId);
            if (r != null && "On Loan".equalsIgnoreCase(r.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public boolean validateBorrowing(String studentId) {
        int count = getActiveBorrowCount(studentId);
        return count < 3;
    }

    public SimpleList<BorrowRecord> getRecordsByStudent(String studentId) {
        SimpleList<BorrowRecord> results = new SimpleList<>();
        SimpleList<String> txIds = studentIndex.read(studentId);
        if (txIds != null) {
            for (int i = 0; i < txIds.size(); i++) {
                BorrowRecord r = mainTree.read(txIds.get(i));
                if (r != null) {
                    results.add(r);
                }
            }
        }
        return results;
    }

    public boolean returnBook(String txId) {
        // 1. Read the borrowing record first
        BorrowRecord r = mainTree.read(txId);

        // 2. If the record does not exist, or the book has already been returned, return failure directly.
        if (r == null || !"On Loan".equalsIgnoreCase(r.getStatus())) {
            return false;
        }

        String oldStatus = r.getStatus();
        IndexHelper.removeFromIndex(statusIndex, oldStatus, txId);

        // 3.Since the record is valid, we can then retrieve the bookId from the record.
        Book book = bookTree.read(r.getBookId());

        if (book != null) {
            // 4. Modify book status
            book.setAvailability("Available");
            bookTree.update(book.getId(), book);
        }

        // 5. Change the record status to "repaid".
        r.setStatus("Returned");
        mainTree.update(txId, r);

        IndexHelper.addToIndex(statusIndex, "Returned", txId);
        return true;
    }

    public boolean markAsOverdue(String txId) {
        BorrowRecord r = mainTree.read(txId);

        if (r == null || !"On Loan".equalsIgnoreCase(r.getStatus())) {
            return false;
        }

        IndexHelper.removeFromIndex(statusIndex, r.getStatus(), txId);

        r.setStatus("Overdue");
        mainTree.update(txId, r);

        IndexHelper.addToIndex(statusIndex, "Overdue", txId);

        return true;
    }

    public BPlusTree.SimpleList<BorrowRecord> getAllRecords() {
        return mainTree.sort();
    }

    private void rebuildAllIndex() {
        this.studentIndex = new BPlusTree<>(10);
        this.statusIndex = new BPlusTree<>(10);
        SimpleList<BorrowRecord> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            BorrowRecord r = all.get(i);
            IndexHelper.addToIndex(statusIndex, r.getStatus(), r.getTransactionId());
            IndexHelper.addToIndex(studentIndex, r.getStudentId(), r.getTransactionId());
        }
    }


    public SimpleList<Object[]> getStatusReport() {
        if (statusIndex == null) {
            return new SimpleList<>();
        }

        SimpleList<String> allStatuses = statusIndex.sortKeys();
        SimpleList<SimpleList<String>> allValueLists = statusIndex.sort();

        // Calculate the total number of records
        int totalRecords = 0;
        for (int i = 0; i < allValueLists.size(); i++) {
            totalRecords += allValueLists.get(i).size();
        }

        // Encapsulation results [Status, Quantity, Percentage]
        SimpleList<Object[]> reportRows = new SimpleList<>();
        for (int i = 0; i < allStatuses.size(); i++) {
            String status = allStatuses.get(i);
            int count = allValueLists.get(i).size();
            double percent = (totalRecords == 0) ? 0 : (count * 100.0 / totalRecords);

            reportRows.add(new Object[]{
                status,
                count,
                String.format("%.2f%%", percent)
            });
        }
        return reportRows;
    }
}
