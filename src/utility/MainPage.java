package utility;

import javax.swing.*;
import java.awt.*;
import boundary.*;
import control.*;

/**
 * Boundary - MainPage
 */
public class MainPage extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);

    private String userRole;
    private String userId;

    public MainPage(String role, String id) {
        this.userRole = role;
        this.userId = id;

        setTitle("Library Management System - " + role);
        setSize(1300, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initLayout();
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        
        BookManager bookManager = new BookManager();
        StudentManager studentManager = new StudentManager();
        StaffManager staffManager = new StaffManager();
        FacilityManager facilityManager = new FacilityManager();
        
        BorrowManager borrowManager = new BorrowManager(bookManager.getTree(), studentManager.getTree());
        BookingManager bookingManager = new BookingManager(facilityManager.getTree());
        
        // --- 1.Sidebar---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 54));
        sidebar.setPreferredSize(new Dimension(220, 800));

        // --- 2. Navigation buttons---
        JButton btnBooks = createMenuBtn("📚 Books Management");
        JButton btnStudents = createMenuBtn("👥 Students Management");
        JButton btnStaff = createMenuBtn("💼 Staff Management");
        JButton btnFacilities = createMenuBtn("🏠 Facilities Management");
        JButton btnBorrow = createMenuBtn("📖 Borrowing services"); 
        JButton btnBooking = createMenuBtn("📅 Booking services"); 
        JButton btnLogout = createMenuBtn("🚪 Log Out");

        // --- 3. Boundaries ---
        BookPanel bookPanel = new BookPanel(bookManager,userRole);
        StudentPanel studentPanel = new StudentPanel(studentManager);
        StaffPanel staffPanel = new StaffPanel(staffManager);
        FacilityPanel facilityPanel = new FacilityPanel(facilityManager);
        BorrowPanel borrowPanel = new BorrowPanel(borrowManager);
        BookingPanel bookingPanel = new BookingPanel(bookingManager);
        
        // --- 4. Add a panel to the CardLayout container ---
        contentPanel.add(bookPanel, "BOOKS");
        contentPanel.add(studentPanel, "STUDENTS");
        contentPanel.add(staffPanel, "STAFF");
        contentPanel.add(facilityPanel, "FACILITIES");
        contentPanel.add(borrowPanel, "BORROW");
        contentPanel.add(bookingPanel, "BOOKING");

        // --- 5. Sidebar assembly (based on role-based control permissions) ---
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(btnBooks);
        sidebar.add(btnFacilities);
        sidebar.add(btnBorrow);
        sidebar.add(btnBooking);

        if (userRole.equals("Staff")) {
            sidebar.add(btnStudents);
            sidebar.add(btnStaff);
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        // --- 6. Navigation switching logic ---
        btnBooks.addActionListener(e -> switchView("BOOKS", bookPanel));
        btnFacilities.addActionListener(e -> switchView("FACILITIES", facilityPanel));
        btnBorrow.addActionListener(e -> switchView("BORROW", borrowPanel));
        btnBooking.addActionListener(e -> switchView("BOOKING", bookingPanel));
        btnStudents.addActionListener(e -> switchView("STUDENTS", studentPanel));
        btnStaff.addActionListener(e -> switchView("STAFF", staffPanel));

        btnLogout.addActionListener(e -> {
            new login().setVisible(true);
            this.dispose();
        });

        // --- 6. Top welcome bar --- 
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(Color.WHITE);
        topBar.add(new JLabel("Current user: " + userId + " [" + userRole + "]  "));

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);
    }

    private void switchView(String name, JPanel panel) {
        cardLayout.show(contentPanel, name);
        // Key: Forces a data refresh when switching, ensuring that the latest data in the B+ tree and file is displayed.
        switch (panel) {
            case BookPanel bookPanel -> bookPanel.refreshData();
            case StudentPanel studentPanel -> studentPanel.refreshData();
            case StaffPanel staffPanel -> staffPanel.refreshData();
            case FacilityPanel facilityPanel -> facilityPanel.refreshData();
            case BorrowPanel borrowPanel -> borrowPanel.refreshData();
            case BookingPanel bookingPanel -> bookingPanel.refreshData();
            default -> {
            }
        }
    }

    private JButton createMenuBtn(String t) {
        JButton b = new JButton(t);
        setupButton(b);
        return b;
    }

    private void setupButton(JButton b) {
        b.setMaximumSize(new Dimension(220, 50));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(45, 52, 54));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
      
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainPage("Staff", "Admin01").setVisible(true);
        });
    }
}
