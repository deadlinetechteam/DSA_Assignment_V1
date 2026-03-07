package utility;

import entitiy.BorrowRecord;
import entitiy.Student;
import entitiy.Book;
import entitiy.Staff;
import adt.BPlusTree;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainPage extends JFrame {
    // 统一静态存储，确保 Staff 和 Student 访问的是同一个 B+ 树
    public static BPlusTree<String, Book> libraryTree = new BPlusTree<>();
    public static BPlusTree<String, Student> studentTree = new BPlusTree<>();
    public static BPlusTree<String, Staff> staffTree = new BPlusTree<>();
    public static BPlusTree<String, BorrowRecord> borrowTree = new BPlusTree<>();
    
    private String userRole, userId;
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    
    private DefaultTableModel bookModel, studentModel, staffModel, borrowListModel;
    private JTable bookTable, studentTable, staffTable, borrowListTable;
    
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private String currentView = "BOOKS";

    private final String[] BOOK_COLS = {"ID*", "Title*", "Availability", "Language", "Authors", "Pub Info", "Edition", "Pub Date", "Doc Type", "Notes"};
    private final String[] STUDENT_COLS = {"ID*", "Name*", "Password*", "Gender", "MykadNO", "Email", "Programme", "Address", "ContactNo"};
    private final String[] STAFF_COLS = {"ID*", "Name*", "Password*", "Location", "Department", "Gender", "Email"};
    private final String[] BORROW_COLS = {"TX ID", "Book ID", "Title", "Student Name", "Borrow Date", "Due Date", "Status"};

    public MainPage(String role, String id) {
        this.userRole = role;
        this.userId = id;

        // 初始化演示数据
        if (libraryTree.search("B001") == null) {
            libraryTree.create("B001", new Book("B001", "Java Programming", "Available", "English", "Deitel", "Pearson", "12e", "2024", "Book", ""));
        }

        setTitle("Library Dashboard - " + role);
        setSize(1400, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initLayout();
        refreshAllData();
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        // --- 顶部搜索 ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterCombo = new JComboBox<>(BOOK_COLS);
        searchField = new JTextField(25);
        topBar.add(new JLabel("🔍 Filter:"));
        topBar.add(filterCombo);
        topBar.add(new JLabel("Search:"));
        topBar.add(searchField);
        topBar.add(new JLabel(" | Welcome: " + userId + " (" + userRole + ")"));

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { refreshAllData(); }
        });

        // --- 侧边栏 ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 54));
        sidebar.setPreferredSize(new Dimension(200, 800));

        JButton btnBooks = createMenuBtn("📚 Book Gallery");
        JButton btnManage = createMenuBtn(userRole.equals("Staff") ? "🛠 Book Data" : "📖 My Borrowing");
        JButton btnLogout = createMenuBtn("🚪 Logout");

        sidebar.add(btnBooks);
        sidebar.add(btnManage);
        
        // 只有 Staff 能看这几个页面
        if (userRole.equals("Staff")) {
            
            JButton btnStd = createMenuBtn("👥 Student Data");
            JButton btnStf = createMenuBtn("💼 Staff Data");
            JButton btnList = createMenuBtn("📋 Global Loans");
            sidebar.add(btnStd);
            sidebar.add(btnStf);
            sidebar.add(btnList);
            
            btnStd.addActionListener(e -> switchView("STUDENTS", STUDENT_COLS));
            btnStf.addActionListener(e -> switchView("STAFF", STAFF_COLS));
            btnList.addActionListener(e -> switchView("BORROW_LIST", BORROW_COLS));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        // --- 内容容器 ---
        contentPanel.add(createDataTablePanel("BOOKS", BOOK_COLS), "BOOKS");
        contentPanel.add(createDataTablePanel("MANAGE_VIEW", userRole.equals("Staff") ? BOOK_COLS : BORROW_COLS), "MANAGE_VIEW");
        if (userRole.equals("Staff")) {
            contentPanel.add(createDataTablePanel("STUDENTS", STUDENT_COLS), "STUDENTS");
            contentPanel.add(createDataTablePanel("STAFF", STAFF_COLS), "STAFF");
            contentPanel.add(createDataTablePanel("BORROW_LIST", BORROW_COLS), "BORROW_LIST");
        }

        add(sidebar, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // 导航切换
        btnBooks.addActionListener(e -> switchView("BOOKS", BOOK_COLS));
        btnManage.addActionListener(e -> switchView("MANAGE_VIEW", userRole.equals("Staff") ? BOOK_COLS : BORROW_COLS));
        btnLogout.addActionListener(e -> { new login().setVisible(true); this.dispose(); });
    }

    private void switchView(String view, String[] cols) {
        this.currentView = view;
        filterCombo.setModel(new DefaultComboBoxModel<>(cols));
        cardLayout.show(contentPanel, view);
        refreshAllData();
    }

    private JPanel createDataTablePanel(String type, String[] cols) {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        if (type.equals("BOOKS")) { bookModel = model; bookTable = table; }
        else if (type.equals("MANAGE_VIEW")) {
            if (userRole.equals("Staff")) { staffModel = model; staffTable = table; } // Staff 模式下作为书库管理
            else { studentModel = model; studentTable = table; } // Student 模式下作为个人借阅管理
        } else if (type.equals("STUDENTS")) { staffModel = model; staffTable = table; } // 借用变量名
        else { borrowListModel = model; borrowListTable = table; }

        // --- 操作按钮 ---
        JPanel bp = new JPanel();
        if (userRole.equals("Staff") && (type.equals("BOOKS") || type.equals("MANAGE_VIEW"))) {
            JButton addB = new JButton("Add");
            JButton upB = new JButton("Update");
            JButton delB = new JButton("Delete");
            addB.addActionListener(e -> showEntryDialog(type, null));
            // ... (省略部分 staff 管理按钮逻辑，同之前一致)
            bp.add(addB); bp.add(upB); bp.add(delB);
        } else if (userRole.equals("Student")) {
            if (type.equals("BOOKS")) {
                JButton btnBorrow = new JButton("Borrow Selected Book");
                btnBorrow.addActionListener(e -> borrowBookLogic());
                bp.add(btnBorrow);
            } else if (type.equals("MANAGE_VIEW")) {
                JButton btnReturn = new JButton("Return Selected Book");
                btnReturn.addActionListener(e -> returnBookLogic());
                bp.add(btnReturn);
            }
        }
        
        p.add(bp, BorderLayout.SOUTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // --- FIXED: 借书逻辑 ---
    private void borrowBookLogic() {
        int r = bookTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a book!"); return; }
        
        String id = (String) bookTable.getValueAt(r, 0);
        Book b = libraryTree.search(id);

        if (b.getAvailability().equalsIgnoreCase("Available")) {
            b.setAvailability("Borrowed by " + userId);
            libraryTree.create(id, b);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Borrowed Successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Book is not available!");
        }
    }

    // --- FIXED: 还书逻辑 ---
    private void returnBookLogic() {
        int r = studentTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a book to return!"); return; }
        
        String id = (String) studentTable.getValueAt(r, 0);
        Book b = libraryTree.search(id);

        if (b != null) {
            b.setAvailability("Available");
            libraryTree.create(id, b);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Returned Successfully!");
        }
    }

    private void refreshAllData() {
        if (bookModel != null) bookModel.setRowCount(0);
        if (studentModel != null) studentModel.setRowCount(0);
        if (borrowListModel != null) borrowListModel.setRowCount(0);

        String q = searchField.getText().toLowerCase().trim();
        BPlusTree.SimpleList<Book> bList = libraryTree.searchRange(" ", "\uffff");

        for (int i = 0; i < bList.size(); i++) {
            Book b = bList.get(i);
            Object[] row = {b.getId(), b.getTitle(), b.getAvailability(), b.getLanguage(), b.getAuthors(), b.getPublicationInformation(), b.getEdition(), b.getPublicationDate(), b.getDocumentType(), b.getContentNotes()};
            
            // 刷新所有书库
            if (bookModel != null) bookModel.addRow(row);
            
            // 核心：如果该书被当前学生借阅，显示在学生管理页
            if (studentModel != null && b.getAvailability().equals("Borrowed by " + userId)) {
                studentModel.addRow(new Object[]{b.getId(), b.getTitle(), userId, "Occupied"});
            }

            // 只有 Staff 看到的全局清单
            if (borrowListModel != null && b.getAvailability().startsWith("Borrowed by")) {
                borrowListModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAvailability().replace("Borrowed by ", ""), "Occupied"});
            }
        }
    }

    private void showEntryDialog(String type, Object exist) {
        String[] cols = type.equals("BOOKS") ? BOOK_COLS : (type.equals("STUDENTS") ? STUDENT_COLS : STAFF_COLS);
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[cols.length];

        for (int i = 0; i < cols.length; i++) {
            pane.add(new JLabel(cols[i] + ":"));
            tfs[i] = new JTextField(exist == null ? "" : getFieldValue(type, exist, i));
            if (i == 0 && exist != null) tfs[i].setEditable(false);
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, type + " Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            // --- 强制验证逻辑 ---
            if (tfs[0].getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "ID is mandatory!"); return; }
            if (tfs[1].getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Name/Title is mandatory!"); return; }
            
            // Password 验证
            if ((type.equals("STUDENTS") || type.equals("STAFF")) && tfs[2].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password is mandatory!");
                return;
            }

            saveData(type, tfs);
            refreshAllData();
        }
    }

    private void saveData(String type, JTextField[] tfs) {
        String id = tfs[0].getText().trim();
        if (type.equals("BOOKS")) {
            libraryTree.create(id, new Book(id, tfs[1].getText(), tfs[2].getText(), tfs[3].getText(), tfs[4].getText(), tfs[5].getText(), tfs[6].getText(), tfs[7].getText(), tfs[8].getText(), tfs[9].getText()));
        } else if (type.equals("STUDENTS")) {
            // --- FIXED: 现在传入 9 个参数，匹配 Student(Id, Name, Password, Gender, MykadNO, Email, Programme, Address, ContactNo) ---
            studentTree.create(id, new Student(id, tfs[1].getText(), tfs[2].getText(), tfs[3].getText(), tfs[4].getText(), tfs[5].getText(), tfs[6].getText(), tfs[7].getText(), tfs[8].getText()));
        } else {
            staffTree.create(id, new Staff(id, tfs[1].getText(), tfs[2].getText(), tfs[3].getText(), tfs[4].getText(), tfs[5].getText(), tfs[6].getText()));
        }
    }

    private String getFieldValue(String type, Object o, int i) {
        if (type.equals("BOOKS")) {
            Book b = (Book)o;
            switch(i) {
                case 0: return b.getId(); case 1: return b.getTitle(); case 2: return b.getAvailability();
                case 3: return b.getLanguage(); case 4: return b.getAuthors(); case 5: return b.getPublicationInformation();
                case 6: return b.getEdition(); case 7: return b.getPublicationDate(); case 8: return b.getDocumentType();
                case 9: return b.getContentNotes(); default: return "";
            }
        } else if (type.equals("STUDENTS")) {
            Student s = (Student)o;
            switch(i) {
                case 0: return s.getId(); case 1: return s.getName(); case 2: return s.getPassword();
                case 3: return s.getGender(); case 4: return s.getMykadNO(); case 5: return s.getEmail();
                case 6: return s.getProgramme(); case 7: return s.getAddress(); case 8: return s.getContactNo();
                default: return "";
            }
        } else {
            Staff st = (Staff)o;
            switch(i) {
                case 0: return st.getId(); case 1: return st.getName(); case 2: return st.getPassword();
                case 3: return st.getLocation(); case 4: return st.getDepartment(); case 5: return st.getGender();
                case 6: return st.getEmail(); default: return "";
            }
        }
    }

    private Object findById(String type, String id) {
        if (type.equals("BOOKS")) return libraryTree.search(id);
        if (type.equals("STUDENTS")) return studentTree.search(id);
        return staffTree.search(id);
    }

    private void deleteById(String type, String id) {
        if (type.equals("BOOKS")) libraryTree.delete(id);
        else if (type.equals("STUDENTS")) studentTree.delete(id);
        else staffTree.delete(id);
    }

    

    private JButton createMenuBtn(String t) {
        JButton b = new JButton(t);
        b.setMaximumSize(new Dimension(200, 50));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }
}