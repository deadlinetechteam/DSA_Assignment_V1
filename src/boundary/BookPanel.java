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
import adt.BPlusTree.SimpleList;
import control.BookManager;
import entitiy.Book;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BookPanel extends JPanel {

    private BookManager bookManager;
    private final DefaultTableModel bookModel;
    private JTable bookTable;

    private final JComboBox<String> comboSearchType;
    private final JTextField txtSearch;
    private final JButton btnSearch;
    private final JButton btnReset;

    private final String[] BOOK_COLS = {"ID*", "Title*", "Availability", "Language", "Authors", "Pub Info", "Edition", "Pub Date", "Doc Type", "Notes"};

    public BookPanel(BookManager bookManager, String userRole) {
        setLayout(new BorderLayout());
        this.bookManager = bookManager;

        // ---1. Table initialization ---
        bookModel = new DefaultTableModel(BOOK_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookTable = new JTable(bookModel);
        bookTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 2. **Initialize the search bar (top)** ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] searchOptions = {"Title", "ID", "Availability"};
        comboSearchType = new JComboBox<>(searchOptions);
        txtSearch = new JTextField(20);
        btnSearch = new JButton("🔍 Search");
        btnReset = new JButton("🔄 Reset");

        searchPanel.add(new JLabel("Search By:"));
        searchPanel.add(comboSearchType);
        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        // --- 3. **Bind search and reset events** ---
        btnSearch.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
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
        populateTable(bookManager.getAllBooks());
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

            if (exist != null) {
                bookManager.updateBook(b);
                JOptionPane.showMessageDialog(this, "Book updated successfully!");
            } else {
                bookManager.createBook(b);
                JOptionPane.showMessageDialog(this, "New book added successfully!");
            }
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
        String keyword = txtSearch.getText().trim(); // Get the keywords and clear the spaces
        String type = (String) comboSearchType.getSelectedItem();
        if (keyword.isEmpty()) {

            refreshData();
            return;
        }
        SimpleList<Book> results;

        switch (type) {
            case "ID" ->
                results = bookManager.searchByID(keyword);
            case "Title" ->
                results = bookManager.searchByTitle(keyword);
            case "Availability" ->
                results = bookManager.searchByAvailability(keyword);
            default ->
                results = bookManager.getAllBooks();
        }
        populateTable(results);
    }

    private void populateTable(BPlusTree.SimpleList<Book> list) {
        bookModel.setRowCount(0);
        if (list == null) {
            return;
        }

        // Loop through the Book object to convert it into table rows.
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
