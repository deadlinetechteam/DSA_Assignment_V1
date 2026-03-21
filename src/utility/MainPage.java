/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package utility;

/**
 *
 * @author asus-z
 */
import javax.swing.*;
import java.awt.*;
import boundary.*;
import control.*;
import entitiy.Staff;
import entitiy.Student;

/**
 * Boundary - MainPage
 */
public class MainPage extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final String userRole;
    private final String userId;

    public MainPage(String role, String id) {
        this.userRole = role;
        this.userId = id;

        setTitle("Library & Facility Management System - [" + role + "]");
        setSize(1300, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initLayout();
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        // --- 1. Initialize all manager---
        BookManager bookManager = GlobalManager.getBookManager();
        StudentManager studentManager = GlobalManager.getStudentManager();
        StaffManager staffManager = GlobalManager.getStaffManager();
        FacilityManager facilityManager = GlobalManager.getFacilityManager();
        BorrowManager borrowManager = GlobalManager.getBorrowManager();
        BookingManager bookingManager = GlobalManager.getBookingManager();

        // --- 2. Initialize all function panels (Boundaries) ---
        BookPanel bookPanel = new BookPanel(bookManager, userRole);
        StudentPanel studentPanel = new StudentPanel(studentManager);
        StaffPanel staffPanel = new StaffPanel(staffManager);
        FacilityPanel facilityPanel = new FacilityPanel(facilityManager, userRole);

        BorrowPanel borrowPanel = new BorrowPanel(borrowManager, bookManager, userId, userRole);
        BookingPanel bookingPanel = new BookingPanel(bookingManager, facilityManager, userId, userRole);

        // --- 3. Sidebar Layout ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 54));
        sidebar.setPreferredSize(new Dimension(240, 800));

        // Create button
        JButton btnBooks = createMenuBtn("📚 Books Management");
        JButton btnFacilities = createMenuBtn("🏠 Facilities Management");
        JButton btnBorrow = createMenuBtn("📖 Borrowing Services");
        JButton btnBooking = createMenuBtn("📅 Booking Services");
        JButton btnStudents = createMenuBtn("👥 Students Management");
        JButton btnStaff = createMenuBtn("💼 Staff Management");
        JButton btnProfile = createMenuBtn("👤 Profile");
        JButton btnLogout = createMenuBtn("🚪 Log Out");

        // --- 4. Assemble the sidebar (access control) ---
        sidebar.add(Box.createVerticalStrut(30)); // Top Spacing
        addSidebarBtn(sidebar, btnBooks, "BOOKS", bookPanel);
        addSidebarBtn(sidebar, btnFacilities, "FACILITIES", facilityPanel);
        addSidebarBtn(sidebar, btnBorrow, "BORROW", borrowPanel);
        addSidebarBtn(sidebar, btnBooking, "BOOKING", bookingPanel);

        if ("Staff".equals(userRole)) {
            addSidebarBtn(sidebar, btnStudents, "STUDENTS", studentPanel);
            addSidebarBtn(sidebar, btnStaff, "STAFF", staffPanel);
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnProfile);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(20));

        // --- 5. Navigation switching logic ---
        btnProfile.addActionListener(e -> showProfileDialog());
        btnLogout.addActionListener(e -> {
            new login().setVisible(true);
            this.dispose();
        });

        // --- 6. Top welcome bar ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JLabel lblUser = new JLabel("Welcome, " + userId + " (" + userRole + ")  ");
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 14));
        topBar.add(lblUser);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // The first screen is displayed by default.
        cardLayout.show(contentPanel, "BOOKS");
    }

    private void addSidebarBtn(JPanel sidebar, JButton btn, String name, JPanel panel) {
        contentPanel.add(panel, name);
        btn.addActionListener(e -> switchView(name, panel));
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(10));
    }

    private void switchView(String name, JPanel panel) {
        cardLayout.show(contentPanel, name);
        switch (panel) {
            case BookPanel p ->
                p.refreshData();
            case StudentPanel p ->
                p.refreshData();
            case StaffPanel p ->
                p.refreshData();
            case FacilityPanel p ->
                p.refreshData();
            case BorrowPanel p ->
                p.refreshData();
            case BookingPanel p ->
                p.refreshData();
            default -> {
            }
        }
    }

    private JButton createMenuBtn(String t) {
        JButton b = new JButton(t);
        b.setMaximumSize(new Dimension(240, 50));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(45, 52, 54));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMargin(new Insets(0, 20, 0, 0));
        return b;
    }

    private void showProfileDialog() {
        String nameVal = "";
        String passVal = "";
        Object userObj = null;

        if ("Staff".equals(userRole)) {
            Staff s = GlobalManager.getStaffManager().readStaff(userId);
            if (s != null) {
                nameVal = s.getName();
                passVal = s.getPassword();
                userObj = s;
            }
        } else {
            Student s = GlobalManager.getStudentManager().readStudent(userId);
            if (s != null) {
                nameVal = s.getName();
                passVal = s.getPassword();
                userObj = s;
            }
        }

        if (userObj == null) {
            JOptionPane.showMessageDialog(this, "Error: User data not found.");
            return;
        }

        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField txtName = new JTextField(nameVal);
        JPasswordField txtPass = new JPasswordField(passVal);

        pane.add(new JLabel("User ID:"));
        pane.add(new JLabel(userId));
        pane.add(new JLabel("Your Name:"));
        pane.add(txtName);
        pane.add(new JLabel("New Password:"));
        pane.add(txtPass);

        int result = JOptionPane.showConfirmDialog(this, pane, "Edit My Profile", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newName = txtName.getText().trim();
            String newPass = new String(txtPass.getPassword());

            if (newName.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Password cannot be empty!");
                return;
            }

            if ("Staff".equals(userRole)) {
                Staff s = (Staff) userObj;
                s.setName(newName);
                s.setPassword(newPass);
                GlobalManager.getStaffManager().updateStaff(s);
            } else {
                Student s = (Student) userObj;
                s.setName(newName);
                s.setPassword(newPass);
                GlobalManager.getStudentManager().updateStudent(s);
            }

            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GlobalManager.init();
            new MainPage("Staff", "S001").setVisible(true);
        });
    }
}
