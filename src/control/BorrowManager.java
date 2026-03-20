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
import java.time.LocalDate;

public class BorrowManager {

    private final BPlusTree<String, BorrowRecord> mainTree;
    private final BPlusTree<String, Student> studentTree;
    private final BPlusTree<String, Book> bookTree;

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
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
              System.out.println("[Auto-Save] Saving borrow record data to disk...");
            if (this.mainTree != null) {
                this.mainTree.commit();
            }
        }));
    }

    public boolean borrowBook(String studentId, String bookId) {
        // 1. 校验学生是否存在
        Student student = studentTree.read(studentId);
        if (student == null) {
            System.out.println("错误：学生 ID 不存在");
            return false;
        }

        // 2. 校验书籍是否存在且可用
        Book book = bookTree.read(bookId);
        if (book == null || !book.getAvailability().equalsIgnoreCase("Available")) {
            return false;
        }

        // 3. 执行借书：修改书态 + 生成记录
        book.setAvailability("Borrowed by " + studentId);
        bookTree.update(bookId, book);

        String txId = "TX" + System.currentTimeMillis();
        BorrowRecord record = new BorrowRecord(
                txId, bookId, book.getTitle(),
                studentId, student.getName(), // 自动获取学生姓名
                java.time.LocalDate.now().toString(),
                java.time.LocalDate.now().plusDays(14).toString(),
                "On Loan"
        );

        mainTree.create(txId, record);

        // 4. 统一提交持久化
        bookTree.commit();
        mainTree.commit();
        return true;
    }

    // 还书逻辑
    public boolean returnBook(String bookId) {
        // 1. 校验书籍是否存在
        Book book = bookTree.read(bookId);
        if (book == null) {
            return false;
        }

        // 2. 将书籍状态恢复为 Available
        book.setAvailability("Available");
        bookTree.update(bookId, book); // 更新书树

        // 3. 寻找并更新借阅记录 (关键修改)
        // 遍历当前所有的借阅记录，找到那条对应的“未还”记录
        BPlusTree.SimpleList<BorrowRecord> allRecords = mainTree.sort(); // 获取有序列表
        boolean recordUpdated = false;

        for (int i = 0; i < allRecords.size(); i++) {
            BorrowRecord r = allRecords.get(i);
            // 匹配逻辑：书籍ID一致 且 状态是 "On Loan"
            if (r.getBookId().equals(bookId) && "On Loan".equalsIgnoreCase(r.getStatus())) {
                r.setStatus("Returned"); // 标记为已归还
                mainTree.update(r.getTransactionId(), r); // 更新记录树
                recordUpdated = true;
                break;
            }
        }

        // 4. 提交持久化
        bookTree.commit(); //
        mainTree.commit();

        return recordUpdated; // 返回是否成功找到了对应的借阅记录并归还
    }

    public BPlusTree.SimpleList<BorrowRecord> getAllRecords() {
        return mainTree.sort();
    }
}
