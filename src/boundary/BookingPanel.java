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
import control.BookingManager;
import control.FacilityManager;
import entitiy.BookingRecord;
import entitiy.Facility;
import entitiy.SlotStatus;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BookingPanel extends JPanel {

    private BookingManager bookingManager;
    private FacilityManager facilityManager;
    private final DefaultTableModel bookingModel;
    private final JTable bookingTable;
    private String startSlot = null;

    private String currentUserId;
    private String userRole;
    private JPanel searchBar;
    private JTextField txtSearchUser;

    private final String[] BOOKING_COLS = {"Booking ID", "User ID", "Facility ID", "Date", "Start Time", "End Time", "Status"};

    public BookingPanel(BookingManager bookingManager, FacilityManager facilityManager, String userID, String userRole) {
        setLayout(new BorderLayout());
        this.bookingManager = bookingManager;
        this.facilityManager = facilityManager;
        this.currentUserId = userID;
        this.userRole = userRole;

        if ("Staff".equals(userRole)) {
            searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            txtSearchUser = new JTextField(15);
            JButton btnSearch = new JButton("🔍 Search Student ID");

            btnSearch.addActionListener(e -> performSearch());
            searchBar.add(new JLabel("Search Booking by Student ID:"));
            searchBar.add(txtSearchUser);
            searchBar.add(btnSearch);
            add(searchBar, BorderLayout.NORTH);
        }
        // --- 1. 表格初始化 (管理视图) ---
        bookingModel = new DefaultTableModel(BOOKING_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookingTable = new JTable(bookingModel);

        // --- 2. 操作按钮 ---
        JPanel bp = new JPanel();
        JButton btnNewBooking = new JButton("➕ New Booking");
        JButton btnCancel = new JButton("❌ Cancel Booking");
        JButton btnRefresh = new JButton("🔄 Refresh List");

        btnNewBooking.addActionListener(e -> showBookingDialog());
        btnCancel.addActionListener(e -> cancelLogic());
        btnRefresh.addActionListener(e -> refreshData());

        bp.add(btnNewBooking);
        bp.add(btnCancel);
        bp.add(btnRefresh);

        add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        bookingModel.setRowCount(0);
        if ("Staff".equals(userRole)) {
            populateTable(bookingManager.getAllBookings());
        } else {
            populateTable(bookingManager.getBookingsByUser(currentUserId));
        }
    }

    private void showBookingDialog() {
        // --- 第一步：前置选择窗口 ---
        JPanel pickPane = new JPanel(new GridLayout(0, 2, 5, 5));

        // 获取所有设施
        SimpleList<Facility> facilities = facilityManager.getAllFacilities();
        if (facilities.size() == 0) {
            JOptionPane.showMessageDialog(this, "No facilities available!");
            return;
        }

        JComboBox<Facility> comboFacility = new JComboBox<>();
        for (int i = 0; i < facilities.size(); i++) {
            comboFacility.addItem(facilities.get(i));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String[] dateOptions = new String[3];
        for (int i = 0; i < 3; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, i);
            dateOptions[i] = sdf.format(cal.getTime());
        }

        JComboBox<String> comboDate = new JComboBox<>(dateOptions);

        pickPane.add(new JLabel("Select Facility:"));
        pickPane.add(comboFacility);
        pickPane.add(new JLabel("Booking Date (YYYY-MM-DD):"));
        pickPane.add(comboDate);

        int step1 = JOptionPane.showConfirmDialog(this, pickPane, "Step 1: Select Facility & Date", JOptionPane.OK_CANCEL_OPTION);

        if (step1 == JOptionPane.OK_OPTION) {
            Facility selectedF = (Facility) comboFacility.getSelectedItem();
            String selectedDate = (String) comboDate.getSelectedItem();

            openTimeGridPicker(selectedF, selectedDate);
        }
    }

    private void openTimeGridPicker(Facility f, String date) {
        JDialog gridDialog = new JDialog((Frame) null, "Select Time Slot for " + f.getName(), true);
        gridDialog.setLayout(new BorderLayout());
        gridDialog.setSize(600, 500);

        SimpleList<SlotStatus> slots = bookingManager.getTimetable(f.getId(), date);
        JButton[] allButtons = new JButton[slots.size()];
        JPanel grid = new JPanel(new GridLayout(0, 4, 5, 5));
        for (int i = 0; i < slots.size(); i++) {
            SlotStatus s = slots.get(i);
            JButton b = new JButton(s.getTimeLabel());
            allButtons[i] = b;
            if (!s.isAvailable()) {
                b.setEnabled(false);
                b.setBackground(s.getReason().equals("Occupied") ? Color.RED : Color.LIGHT_GRAY);
            } else {
                b.setBackground(Color.GREEN);
                b.addActionListener(e -> {

                    handleSlotClick(s.getTimeLabel(), f, date, gridDialog, allButtons, slots);
                });
            }
            grid.add(b);
        }

        gridDialog.add(new JScrollPane(grid), BorderLayout.CENTER);
        gridDialog.setLocationRelativeTo(this);
        gridDialog.setVisible(true);
    }

    // 辅助：由于选的是格子起点，结束时间是该起点 +30min
    private String calculateEndTime(String startTime) {
        String[] parts = startTime.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        m += 30;
        if (m >= 60) {
            h++;
            m = 0;
        }
        return String.format("%02d:%02d", h, m);
    }

    private void cancelLogic() {
        int r = bookingTable.getSelectedRow();
        if (r == -1) {
            return;
        }
        String bId = (String) bookingTable.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Cancel " + bId + "?") == JOptionPane.YES_OPTION) {
            bookingManager.cancelBooking(bId);
            refreshData();
        }
    }

    private void handleSlotClick(String clickedTime, Facility f, String date, JDialog gridDialog, JButton[] allButtons, SimpleList<SlotStatus> slots) {
        if (startSlot == null) {
            startSlot = clickedTime;
            updateGridColors(allButtons, slots,clickedTime);
        } else {
            String endSlot = calculateEndTime(clickedTime); // 补足最后 30 分钟

            // 逻辑 A: 检查是否点回去了
            if (clickedTime.compareTo(startSlot) < 0) {
                startSlot = clickedTime;
                updateGridColors(allButtons, slots, null);
                return;
            }

            // --- 核心修改点：调用 Manager 检查时长 ---
            if (!bookingManager.isDurationValid(startSlot, endSlot)) {
                JOptionPane.showMessageDialog(gridDialog,
                        "Booking limit exceeded! Maximum 2 hours (4 slots) per booking.",
                        "Time Limit", JOptionPane.WARNING_MESSAGE);

                startSlot = null; // 重置选择
                updateGridColors(allButtons, slots, null);
                return;
            }

            // 逻辑 B: 检查中间是否有冲突（红格）
            if (bookingManager.isRangeAvailable(f.getId(), date, startSlot, endSlot)) {
                confirmFinalBooking(f, date, startSlot, endSlot, gridDialog);
            } else {
                JOptionPane.showMessageDialog(gridDialog, "Conflict detected in selected range!");
                startSlot = null;
                updateGridColors(allButtons, slots, null);
            }
        }
    }

    private void updateGridColors(JButton[] buttons, SimpleList<SlotStatus> slots, String currentClick) {
        if (buttons == null || slots == null) {
            return;
        }

        for (int i = 0; i < slots.size(); i++) {
            if (i >= buttons.length || buttons[i] == null) {
                continue;
            }

            // 只有可用的格子参与变色逻辑
            if (slots.get(i).isAvailable()) {
                String slotTime = slots.get(i).getTimeLabel();

                if (startSlot != null && currentClick != null) {
                    // 如果已经选了起点，且正在尝试选终点
                    // 将 [startSlot, currentClick] 之间的格子全部涂黄
                    if (slotTime.compareTo(startSlot) >= 0 && slotTime.compareTo(currentClick) <= 0) {
                        buttons[i].setBackground(Color.YELLOW);
                    } else {
                        buttons[i].setBackground(Color.GREEN);
                    }
                } else if (startSlot != null && slotTime.equals(startSlot)) {
                    // 只选了起点
                    buttons[i].setBackground(Color.YELLOW);
                } else {
                    // 没选或已重置
                    buttons[i].setBackground(Color.GREEN);
                }
            }
        }
    }

    private void confirmFinalBooking(Facility f, String date, String start, String end, JDialog gridDialog) {
        String userId;
        if ("Staff".equals(userRole)) {
            // 如果是 Staff 帮学生订，还是需要输入学生 ID
            userId = JOptionPane.showInputDialog(gridDialog, "Booking for Student (Enter ID):");
        } else {
            // 如果是 Student 自己订，直接拿 currentUserId，一秒都不耽误
            userId = this.currentUserId;
        }

        if (userId != null && !userId.trim().isEmpty()) {
            boolean success = bookingManager.makeBooking(userId.trim(), f.getId(), date, start, end);
            if (success) {
                JOptionPane.showMessageDialog(gridDialog, "Booking Confirmed!");
                gridDialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(gridDialog, "Booking Failed. Please try again.");
            }
        }
        startSlot = null; // 无论成功失败，操作结束都要重置
    }

    private void performSearch() {
        String searchId = txtSearchUser.getText().trim();
        if (searchId.isEmpty()) {
            refreshData();
            return;
        }
        bookingModel.setRowCount(0);
        SimpleList<BookingRecord> results = bookingManager.getBookingsByUser(searchId);
        populateTable(results);
    }

    private void populateTable(BPlusTree.SimpleList<BookingRecord> list) {
        bookingModel.setRowCount(0);
        if (list == null) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            BookingRecord r = list.get(i);
            bookingModel.addRow(new Object[]{
                r.getId(), r.getUserId(), r.getFacilityId(),
                r.getBookingDate(), r.getStartTime(), r.getEndTime(), r.getStatus()
            });
        }

    }

}
