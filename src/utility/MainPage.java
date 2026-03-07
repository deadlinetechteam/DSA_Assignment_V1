package utility;

import javax.swing.*;
import java.awt.*;
import boundary.*;

/**
 * 最终整合版 Boundary - MainPage
 * 负责主窗体布局、侧边栏导航以及各实体面板的调度
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

        // --- 1. 侧边栏 (Sidebar) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 54));
        sidebar.setPreferredSize(new Dimension(220, 800));

        // 导航按钮
        JButton btnBooks = createMenuBtn("📚 图书管理");
        JButton btnStudents = createMenuBtn("👥 学生管理");
        JButton btnStaff = createMenuBtn("💼 员工管理");
        JButton btnFacilities = createMenuBtn("🏠 设施管理");
        JButton btnBorrow = new JButton("📖 借还业务"); // 简化版按钮
        JButton btnBooking = new JButton("📅 预约业务"); // 简化版按钮
        JButton btnLogout = createMenuBtn("🚪 退出登录");

        // 样式微调
        setupButton(btnBorrow);
        setupButton(btnBooking);

        // --- 2. 实例化所有实体面板 (Boundaries) ---
        BookPanel bookPanel = new BookPanel();
        StudentPanel studentPanel = new StudentPanel();
        StaffPanel staffPanel = new StaffPanel();
        FacilityPanel facilityPanel = new FacilityPanel();
        BorrowPanel borrowPanel = new BorrowPanel();
        BookingPanel bookingPanel = new BookingPanel();

        // --- 3. 将面板添加到 CardLayout 容器 ---
        contentPanel.add(bookPanel, "BOOKS");
        contentPanel.add(studentPanel, "STUDENTS");
        contentPanel.add(staffPanel, "STAFF");
        contentPanel.add(facilityPanel, "FACILITIES");
        contentPanel.add(borrowPanel, "BORROW");
        contentPanel.add(bookingPanel, "BOOKING");

        // --- 4. 侧边栏组装 (根据角色控制权限) ---
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

        // --- 5. 导航切换逻辑 ---
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

        // --- 6. 顶部欢迎条 ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(Color.WHITE);
        topBar.add(new JLabel("当前用户: " + userId + " [" + userRole + "]  "));

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);
    }

    private void switchView(String name, JPanel panel) {
        cardLayout.show(contentPanel, name);
        // 关键：切换时强制刷新数据，确保看到的是 B+ 树和文件中的最新数据
        if (panel instanceof BookPanel) ((BookPanel) panel).refreshData();
        else if (panel instanceof StudentPanel) ((StudentPanel) panel).refreshData();
        else if (panel instanceof StaffPanel) ((StaffPanel) panel).refreshData();
        else if (panel instanceof FacilityPanel) ((FacilityPanel) panel).refreshData();
        else if (panel instanceof BorrowPanel) ((BorrowPanel) panel).refreshData();
        else if (panel instanceof BookingPanel) ((BookingPanel) panel).refreshData();
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
        // 程序入口
        SwingUtilities.invokeLater(() -> {
            // 假设以管理员身份进入
            new MainPage("Staff", "Admin01").setVisible(true);
        });
    }
}