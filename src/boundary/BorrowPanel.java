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
import java.time.LocalDate;

public class BorrowPanel extends JPanel {

    private BorrowManager borrowManager;
    private BookManager bookManager;
    private String currentUserId;
    private String userRole;
    private boolean isStaff;

    // 表格模型
    private DefaultTableModel bookModel;
    private DefaultTableModel recordModel;
    private JTable bookTable;
    private JTable recordTable;

    public BorrowPanel(BorrowManager borrowManager, BookManager bookManager, String userID, String userRole) {
        this.borrowManager = borrowManager;
        this.bookManager = bookManager;
        this.currentUserId = userID;
        this.userRole = userRole;
        this.isStaff = "Staff".equalsIgnoreCase(userRole);

        setLayout(new BorderLayout());

        // 使用 TabbedPane 分成两个视图
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📚 Browse Catalog", createCatalogTab());
        tabbedPane.addTab("📋 Records", createRecordsTab());

        add(tabbedPane, BorderLayout.CENTER);
        refreshData();
    }

    // --- Tab 1: 图书馆书架 (借书区) ---
    private JPanel createCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout());

        // 书架表格
        String[] cols = {"Book ID", "Title", "Author", "Language", "Status"};
        bookModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookTable = new JTable(bookModel);

        // 借书按钮
        JButton btnBorrow = new JButton("Confirm Borrowing Selected Book");
        btnBorrow.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnBorrow.setBackground(new Color(46, 204, 113));
        btnBorrow.setForeground(Color.WHITE);

        btnBorrow.addActionListener(e -> borrowAction());

        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        panel.add(btnBorrow, BorderLayout.SOUTH);

        return panel;
    }

    // --- Tab 2: 借阅记录 (还书/逾期区) ---
    private JPanel createRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"TX ID", "Book ID", "Title", "Student ID", "Borrow Date", "Due Date", "Status"};
        recordModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        recordTable = new JTable(recordModel);

        // --- 核心功能：逾期变红渲染器 ---
        recordTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);

                String dueDateStr = (String) t.getValueAt(r, 5); // Due Date 列
                String status = (String) t.getValueAt(r, 6);    // Status 列

                // 逻辑：如果是 On Loan 且 今天日期 > Due Date
                if ("On Loan".equalsIgnoreCase(status)
                        && LocalDate.now().toString().compareTo(dueDateStr) > 0) {
                    comp.setForeground(Color.RED);
                    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                } else {
                    comp.setForeground(Color.BLACK);
                    comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
                }
                return comp;
            }
        });

        // 按钮栏
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

    // --- 逻辑：刷新数据 ---
    public void refreshData() {
        // 1. 刷新书架
        bookModel.setRowCount(0);
        SimpleList<Book> books = bookManager.getAllBooks();
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthors(), b.getLanguage(), b.getAvailability()});
        }

        // 2. 刷新记录 (根据角色过滤)
        recordModel.setRowCount(0);
        SimpleList<BorrowRecord> records;
        if (isStaff) {
            records = borrowManager.getAllRecords();
        } else {
            // 需要你在 BorrowManager 里实现这个按 ID 过滤的方法
            records = borrowManager.getRecordsByStudent(currentUserId);
        }

        for (int i = 0; i < records.size(); i++) {
            BorrowRecord r = records.get(i);
            recordModel.addRow(new Object[]{
                r.getTransactionId(), r.getBookId(), r.getBookTitle(),
                r.getStudentId(), r.getBorrowDate(), r.getDueDate(), r.getStatus()
            });
        }
    }

    // --- 逻辑：借书动作 ---
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

        // 1. 检查借书上限 (需要 BorrowManager 支持)
        if (borrowManager.getActiveBorrowCount(studentId) >= 3) {
            JOptionPane.showMessageDialog(this, "Borrowing Failed: This student already has 3 books!");
            return;
        }

        // 2. 执行借书
        if (borrowManager.borrowBook(studentId, bId)) {
            JOptionPane.showMessageDialog(this, "Success! Please return within 14 days.");
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "System Error.");
        }
    }

    // --- 逻辑：还书动作 ---
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
}
