/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 *
 * @author asus-z
 */
import adt.BPlusTree.SimpleList;
import control.BookManager;
import control.BorrowManager;
import entitiy.Book;
import entitiy.BorrowRecord;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BorrowPanel extends JPanel {

    private final BorrowManager borrowManager;
    private final BookManager bookManager;
    private final String currentUserId;
    private final String userRole;
    private final boolean isStaff;

    private DefaultTableModel bookModel;
    private DefaultTableModel recordModel;
    private JTable bookTable;
    private JTable recordTable;
    private JTextField txtSearchId;

    private DefaultTableModel reportModel;
    private JLabel lblTotalBorrowings;

    public BorrowPanel(BorrowManager borrowManager, BookManager bookManager, String userID, String userRole) {
        this.borrowManager = borrowManager;
        this.bookManager = bookManager;
        this.currentUserId = userID;
        this.userRole = userRole;
        this.isStaff = "Staff".equalsIgnoreCase(userRole);

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📚 Browse Catalog", createCatalogTab());
        tabbedPane.addTab("📋 Records", createRecordsTab());

        if (isStaff) {
            tabbedPane.addTab("📈 Status Report", createReportTab());
        }

        add(tabbedPane, BorderLayout.CENTER);
        refreshData();
    }

    // --- Tab 1:Library bookshelves (borrowing area) ---
    private JPanel createCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"Book ID", "Title", "Author", "Category", "Status"};
        bookModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookTable = new JTable(bookModel);

        // Book borrowing button
        JButton btnBorrow = new JButton("Confirm Borrowing Selected Book");
        btnBorrow.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnBorrow.setBackground(new Color(46, 204, 113));
        btnBorrow.setForeground(Color.WHITE);

        btnBorrow.addActionListener(e -> borrowAction());

        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        panel.add(btnBorrow, BorderLayout.SOUTH);

        return panel;
    }

    // --- Tab 2: Borrowing history (returned/overdue section) ---
    private JPanel createRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout());

        if (isStaff) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.add(new JLabel("Search Student ID:"));
            txtSearchId = new JTextField(15);
            JButton btnSearch = new JButton("Search");
            JButton btnClear = new JButton("Clear");
            JButton btnMarkOverdue = new JButton("⚠️ Mark Overdue");
            btnMarkOverdue.setBackground(new Color(231, 76, 60));
            btnMarkOverdue.setForeground(Color.WHITE);
            searchPanel.add(txtSearchId);
            searchPanel.add(btnSearch);
            searchPanel.add(btnClear);
            searchPanel.add(btnMarkOverdue);
            btnSearch.addActionListener(e -> searchAction());
            btnClear.addActionListener(e -> {
                txtSearchId.setText("");
                refreshData();
            });
            btnMarkOverdue.addActionListener(e -> {
                int row = recordTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select a record first!");
                    return;
                }

                String txId = (String) recordTable.getValueAt(row, 0);
                String status = (String) recordTable.getValueAt(row, 6);

                if (!"On Loan".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Only 'On Loan' records can be marked as overdue!");
                    return;
                }

                if (borrowManager.markAsOverdue(txId)) {
                    JOptionPane.showMessageDialog(this, "Status updated to Overdue!");
                    refreshData();
                }
            });

            panel.add(searchPanel, BorderLayout.NORTH);
        }

        String[] cols = {"TX ID", "Book ID", "Title", "Student ID", "Borrow Date", "Due Date", "Status"};
        recordModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        recordTable = new JTable(recordModel);

        // --- Overdue red renderer ---
        recordTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);

                String status = (String) t.getValueAt(r, 6);    // Status 

                boolean isExplicitOverdue = "Overdue".equalsIgnoreCase(status);
                if (isExplicitOverdue) {
                    comp.setForeground(Color.RED);
                    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                } else {
                    comp.setForeground(Color.BLACK);
                    comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
                }

                return comp;
            }
        });

        // Button bar
        JPanel bp = new JPanel();
        JButton btnReturn = new JButton("Return Book");
        JButton btnRefresh = new JButton("Refresh All");
        btnReturn.addActionListener(e -> returnAction());
        btnRefresh.addActionListener(e -> refreshData());
        bp.add(btnReturn);
        bp.add(btnRefresh);

        panel.add(new JScrollPane(recordTable), BorderLayout.CENTER);
        panel.add(bp, BorderLayout.SOUTH);

        return panel;
    }

    // ---  Tab 3: Borrowing Status Distribution Report ---
    private JPanel createReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTotalBorrowings = new JLabel("Total Circulation Records: 0");
        lblTotalBorrowings.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(lblTotalBorrowings, BorderLayout.NORTH);

        String[] cols = {"Borrow Status", "Count", "Percentage (%)"};
        reportModel = new DefaultTableModel(cols, 0);
        JTable reportTable = new JTable(reportModel);

        reportTable.setRowHeight(30);
        reportTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        // 1. Refresh Book Catalog
        bookModel.setRowCount(0);
        SimpleList<Book> books = bookManager.getAllBooks();
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthors(), b.getCategory(), b.getAvailability()});
        }

        // 2. Break personal/school record
        SimpleList<BorrowRecord> records;
        if (isStaff) {
            records = borrowManager.getAllRecords();
        } else {
            records = borrowManager.getRecordsByStudent(currentUserId);
        }
        populateTable(records);

        // 3. If it's a staff member, refresh the report.
        if (isStaff && reportModel != null) {
            updateReport();
        }
    }

    private void updateReport() {
        reportModel.setRowCount(0);
        SimpleList<Object[]> stats = borrowManager.getStatusReport();
        int total = 0;

        if (stats != null) {
            for (int i = 0; i < stats.size(); i++) {
                Object[] row = stats.get(i);
                reportModel.addRow(row);
                total += (int) row[1];
            }
        }
        lblTotalBorrowings.setText("Total Circulation Records: " + total);
    }

    private void populateTable(SimpleList<BorrowRecord> list) {
        recordModel.setRowCount(0);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            BorrowRecord r = list.get(i);
            recordModel.addRow(new Object[]{
                r.getTransactionId(), r.getBookId(), r.getBookTitle(),
                r.getStudentId(), r.getBorrowDate(), r.getDueDate(), r.getStatus()
            });
        }
    }

    private void borrowAction() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book from the shelf!");
            return;
        }

        String bId = (String) bookTable.getValueAt(row, 0);
        String status = (String) bookTable.getValueAt(row, 4);

        if (!"Available".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This book is currently out.");
            return;
        }

        String studentId = currentUserId;
        if (isStaff) {
            studentId = JOptionPane.showInputDialog(this, "Enter Student ID to borrow for:");
            if (studentId == null || studentId.trim().isEmpty()) {
                return;
            }
        }

        // 1. Check the borrowing limit
        if (!borrowManager.validateBorrowing(studentId)) {
            JOptionPane.showMessageDialog(this, "Borrowing Failed: This student already has 3 books!");
            return;
        }

        // 2. Implementing book lending
        if (borrowManager.borrowBook(studentId, bId)) {
            JOptionPane.showMessageDialog(this, "Success! Please return within 14 days.");
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "System Error.");
        }
    }

    private void returnAction() {
        int row = recordTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a record to return!");
            return;
        }

        String status = (String) recordTable.getValueAt(row, 6);
        if ("Returned".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This book is already returned!");
            return;
        }

        String recordId = (String) recordTable.getValueAt(row, 0);
        if (borrowManager.returnBook(recordId)) {
            JOptionPane.showMessageDialog(this, "Book Returned Successfully!");
            refreshData();
        }
    }

    private void searchAction() {
        String keyword = txtSearchId.getText().trim();
        if (keyword.isEmpty()) {
            refreshData();
            return;
        }
        SimpleList<BorrowRecord> results = borrowManager.getRecordsByStudent(keyword);
        populateTable(results);
    }

}
