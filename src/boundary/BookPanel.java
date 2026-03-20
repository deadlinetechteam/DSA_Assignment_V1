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

    private BookManager bookManager;
    private final DefaultTableModel bookModel;
    private JTable bookTable;

    private final JTextField txtSearch;
    private final JButton btnSearch;
    private final JButton btnReset;

    private final String[] BOOK_COLS = {"ID*", "Title*", "Availability", "Language", "Authors", "Pub Info", "Edition", "Pub Date", "Doc Type", "Notes"};

    public BookPanel(BookManager bookManager, String userRole) {
        setLayout(new BorderLayout());
        this.bookManager = bookManager;

        // --- Table initialization ---
        bookModel = new DefaultTableModel(BOOK_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookTable = new JTable(bookModel);
        bookTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

// --- 2. **初始化搜索栏 (顶部)** ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] searchOptions = {"Title", "ID", "Availability"};
        JComboBox<String> comboSearchType = new JComboBox<>(searchOptions);
        txtSearch = new JTextField(20); 
        btnSearch = new JButton("🔍 Search");
        btnReset = new JButton("🔄 Reset");

        searchPanel.add(new JLabel("Search By:"));
        searchPanel.add(comboSearchType); 
        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        // --- 3. **绑定搜索和重置事件** ---
        btnSearch.addActionListener(e -> performSearch()); // 点击搜索
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); // 清空输入框
            refreshData(); // 重新加载全量数据
        });

        // --- Operation button panel ---
        if ("Staff".equals(userRole)) {
            JPanel bp = new JPanel();

            JButton addB = new JButton("Add Book");
            JButton upB = new JButton("Update Book");
            JButton delB = new JButton("Delete Book");

            // Button event binding
            addB.addActionListener(e -> showEntryDialog(null));
            upB.addActionListener(e -> {
                int r = bookTable.getSelectedRow();
                if (r != -1) {
                    String id = (String) bookTable.getValueAt(r, 0);
                    showEntryDialog(bookManager.readBook(id));
                } else {
                    JOptionPane.showMessageDialog(this, "Select a book to update!");
                }
            });
            delB.addActionListener(e -> deleteLogic());

            bp.add(addB);
            bp.add(upB);
            bp.add(delB);

            add(bp, BorderLayout.SOUTH);
        }
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);
        refreshData();
    }

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

    private void showEntryDialog(Book exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[BOOK_COLS.length];

        for (int i = 0; i < BOOK_COLS.length; i++) {
            pane.add(new JLabel(BOOK_COLS[i] + ":"));
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            if (exist == null && i == 2) {
                val = "Available";
            }
            tfs[i] = new JTextField(val);
            if (i == 0 && exist != null) {
                tfs[i].setEditable(false);
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

            bookManager.createBook(b);
            refreshData();
        }
    }

    private String getFieldValue(Book b, int i) {
        return switch (i) {
            case 0 ->
                b.getId();
            case 1 ->
                b.getTitle();
            case 2 ->
                b.getAvailability();
            case 3 ->
                b.getLanguage();
            case 4 ->
                b.getAuthors();
            case 5 ->
                b.getPublicationInformation();
            case 6 ->
                b.getEdition();
            case 7 ->
                b.getPublicationDate();
            case 8 ->
                b.getDocumentType();
            case 9 ->
                b.getContentNotes();
            default ->
                "";
        };
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim(); // 拿到关键词并清空空格
        if (keyword.isEmpty()) {
            // 如果关键词是空的，就当是重置，加载全量数据
            refreshData();
            return;
        }

        // 6. **去数据仓库筛选 (这一步需要 Manager 的支持)**
        // 假设你的 B+ 树提供了一个简单的包含查询方法
        // result 需要是一个 SimpleList<Book> 类型，这和你 `refreshData` 里的类型一致
        var result = bookManager.searchByTitle(keyword);

        // 7. **将筛选结果重新画到表格上 (复用刷新逻辑)**
        populateTable(result);
    }

    private void populateTable(BPlusTree.SimpleList<Book> list) {
        bookModel.setRowCount(0); // 擦黑板
        if (list == null) {
            return;
        }

        // 循环将 Book 对象转换为表格行
        for (int i = 0; i < list.size(); i++) {
            Book b = list.get(i);
            bookModel.addRow(new Object[]{
                b.getId(), b.getTitle(), b.getAvailability(),
                b.getLanguage(), b.getAuthors(), b.getPublicationInformation(),
                b.getEdition(), b.getPublicationDate(), b.getDocumentType(), b.getContentNotes()
            });
        }
    }

}
