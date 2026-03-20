/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 *
 * @author asus-z
 */
import adt.BPlusTree;
import control.BorrowManager;
import entitiy.BorrowRecord;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BorrowPanel extends JPanel {

    private BorrowManager borrowManager;
    private final DefaultTableModel borrowModel;
    private final JTable borrowTable;

    private final String[] BORROW_COLS = {"TX ID", "Book ID", "Title", "Student ID", "Borrow Date", "Due Date", "Status"};

    public BorrowPanel(BorrowManager borrowManager) {
        setLayout(new BorderLayout());
        this.borrowManager = borrowManager;
        // --- table initialization ---
        borrowModel = new DefaultTableModel(BORROW_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        borrowTable = new JTable(borrowModel);

        // --- 操作按钮 ---
        JPanel bp = new JPanel();
        JButton btnBorrow = new JButton("New Borrowing");
        JButton btnReturn = new JButton("Return Book");
        JButton btnRefresh = new JButton("Refresh");

        btnBorrow.addActionListener(e -> showBorrowDialog());
        btnReturn.addActionListener(e -> returnLogic());
        btnRefresh.addActionListener(e -> refreshData());

        bp.add(btnBorrow);
        bp.add(btnReturn);
        bp.add(btnRefresh);

        add(new JScrollPane(borrowTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        borrowModel.setRowCount(0);
        // 从 BorrowManager 获取所有借阅记录
        BPlusTree.SimpleList<BorrowRecord> list = borrowManager.getAllRecords();
        for (int i = 0; i < list.size(); i++) {
            BorrowRecord r = list.get(i);
            borrowModel.addRow(new Object[]{
                r.getTransactionId(), r.getBookId(), r.getBookTitle(),
                r.getStudentId(), r.getBorrowDate(), r.getDueDate(), r.getStatus()
            });
        }
    }

    // 借书对话框逻辑
    private void showBorrowDialog() {
        // 1. 准备容器和布局
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));

        // 2. 处理学生 ID (如果是学生登录，直接锁定 ID，不让改)
        // 这里的 currentUserId 需要从 MainPage 传给 BorrowPanel 构造函数
        JTextField txtStudentId = new JTextField(currentUserId);
        txtStudentId.setEditable(false);

        // 3. 核心：创建图书选择下拉框
        JComboBox<String> bookSelector = new JComboBox<>();

        // 从 BookManager 获取所有状态为 "Available" 的书籍
        // 假设你有一个方法获取 SimpleList<Book>
        var availableBooks = bookManager.getAllBooks();
        int count = 0;
        for (int i = 0; i < availableBooks.size(); i++) {
            var b = availableBooks.get(i);
            // 只把能借的书加进下拉框
            if ("Available".equalsIgnoreCase(b.getStatus())) {
                bookSelector.addItem(b.getId() + " - " + b.getTitle());
                count++;
            }
        }

        // 4. 组装弹窗界面
        pane.add(new JLabel("Student ID:"));
        pane.add(txtStudentId);
        pane.add(new JLabel("Select Book:"));

        if (count == 0) {
            pane.add(new JLabel("No books available!"));
        } else {
            pane.add(bookSelector);
        }

        // 5. 显示对话框
        int result = JOptionPane.showConfirmDialog(this, pane, "New Borrowing", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION && count > 0) {
            // 拿到选中的字符串，例如 "B001 - Java Programming"
            String selectedItem = (String) bookSelector.getSelectedItem();
            // 拆分出 ID
            String bId = selectedItem.split(" - ")[0];
            String sId = txtStudentId.getText().trim();

            // 6. 执行借书逻辑
            boolean success = borrowManager.borrowBook(sId, bId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Borrowing Successful!");
                refreshData(); // 刷新当前的借阅记录表格
            } else {
                JOptionPane.showMessageDialog(this, "System Error: Please try again.");
            }
        }
    }

    // 还书逻辑
    private void returnLogic() {
        int r = borrowTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a record to return!");
            return;
        }

        String bookId = (String) borrowTable.getValueAt(r, 1);
        // 调用 BorrowManager 处理还书逻辑 (恢复 Book 状态)
        boolean success = borrowManager.returnBook(bookId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Book Returned!");
            refreshData();
        }
    }
}
