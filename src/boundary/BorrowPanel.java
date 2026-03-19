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
import control.BookManager;
import entitiy.Book;
import entitiy.BorrowRecord;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BorrowPanel extends JPanel {

    private BorrowManager borrowManager = new BorrowManager();
    private BookManager bookManager = new BookManager(); // 用于在借书时选择书籍
    private DefaultTableModel borrowModel;
    private JTable borrowTable;

    private final String[] BORROW_COLS = {"TX ID", "Book ID", "Title", "Student ID", "Borrow Date", "Due Date", "Status"};

    public BorrowPanel() {
        setLayout(new BorderLayout());

        // --- 表格初始化 ---
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
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField txtStudentId = new JTextField();
        JTextField txtStudentName = new JTextField();
        JTextField txtBookId = new JTextField();

        pane.add(new JLabel("Student ID:"));
        pane.add(txtStudentId);
        pane.add(new JLabel("Student Name:"));
        pane.add(txtStudentName);
        pane.add(new JLabel("Book ID:"));
        pane.add(txtBookId);

        int result = JOptionPane.showConfirmDialog(null, pane, "New Borrowing", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String sId = txtStudentId.getText().trim();
            String sName = txtStudentName.getText().trim();
            String bId = txtBookId.getText().trim();

            // 调用 BorrowManager 处理借书逻辑 (联动 Book 状态更新与 Record 生成)
            boolean success = borrowManager.borrowBook(sId, sName, bId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Borrowing Successful!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed: Book not available or not found.");
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
