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
import control.BookManager;
import entitiy.Book;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BookPanel extends JPanel {

    private BookManager bookManager = new BookManager();
    private DefaultTableModel bookModel;
    private JTable bookTable;

    // 匹配你之前定义的 BOOK_COLS
    private final String[] BOOK_COLS = {"ID*", "Title*", "Availability", "Language", "Authors", "Pub Info", "Edition", "Pub Date", "Doc Type", "Notes"};

    public BookPanel() {
        setLayout(new BorderLayout());

        // --- 表格初始化 ---
        bookModel = new DefaultTableModel(BOOK_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookTable = new JTable(bookModel);
        bookTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 操作按钮面板 ---
        JPanel bp = new JPanel();
        JButton addB = new JButton("Add Book");
        JButton upB = new JButton("Update Book");
        JButton delB = new JButton("Delete Book");

        // 按钮事件绑定
        addB.addActionListener(e -> showEntryDialog(null));
        upB.addActionListener(e -> {
            int r = bookTable.getSelectedRow();
            if (r != -1) {
                String id = (String) bookTable.getValueAt(r, 0);
                showEntryDialog(bookManager.getBook(id));
            } else {
                JOptionPane.showMessageDialog(this, "Select a book to update!");
            }
        });
        delB.addActionListener(e -> deleteLogic());

        bp.add(addB);
        bp.add(upB);
        bp.add(delB);

        add(new JScrollPane(bookTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        // 初始加载数据
        refreshData();
    }

    // 从 Control 层重新加载数据并刷新表格
    public void refreshData() {
        bookModel.setRowCount(0);
        BPlusTree.SimpleList<Book> bList = bookManager.getAllBooks();

        for (int i = 0; i < bList.size(); i++) {
            Book b = bList.get(i);
            bookModel.addRow(new Object[]{
                b.getId(), b.getTitle(), b.getAvailability(), b.getLanguage(),
                b.getAuthors(), b.getPublicationInformation(), b.getEdition(),
                b.getPublicationDate(), b.getDocumentType(), b.getContentNotes()
            });
        }
    }

    // 删除逻辑：调用 Manager 触发 B+ 树删除平衡
    private void deleteLogic() {
        int r = bookTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a book to delete!");
            return;
        }

        String id = (String) bookTable.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Confirm delete Book: " + id + "?", "Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            bookManager.deleteBook(id);
            refreshData();
        }
    }

    // 录入/编辑对话框
    private void showEntryDialog(Book exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[BOOK_COLS.length];

        for (int i = 0; i < BOOK_COLS.length; i++) {
            pane.add(new JLabel(BOOK_COLS[i] + ":"));
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            tfs[i] = new JTextField(val);
            if (i == 0 && exist != null) {
                tfs[i].setEditable(false); // ID 不可编辑
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Book Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (tfs[0].getText().trim().isEmpty() || tfs[1].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Title are mandatory!");
                return;
            }

            Book b = new Book(
                    tfs[0].getText().trim(), tfs[1].getText().trim(), tfs[2].getText().trim(),
                    tfs[3].getText().trim(), tfs[4].getText().trim(), tfs[5].getText().trim(),
                    tfs[6].getText().trim(), tfs[7].getText().trim(), tfs[8].getText().trim(),
                    tfs[9].getText().trim()
            );

            bookManager.saveBook(b); // 存入 B+ 树并写入磁盘
            refreshData();
        }
    }

    private String getFieldValue(Book b, int i) {
        switch (i) {
            case 0:
                return b.getId();
            case 1:
                return b.getTitle();
            case 2:
                return b.getAvailability();
            case 3:
                return b.getLanguage();
            case 4:
                return b.getAuthors();
            case 5:
                return b.getPublicationInformation();
            case 6:
                return b.getEdition();
            case 7:
                return b.getPublicationDate();
            case 8:
                return b.getDocumentType();
            case 9:
                return b.getContentNotes();
            default:
                return "";
        }
    }
}
