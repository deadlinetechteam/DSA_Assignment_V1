/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 *
 * @author asus-z
 */
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

public class BookingPanel extends JPanel {

    private BookingManager bookingManager;
    private FacilityManager facilityManager;
    private final DefaultTableModel bookingModel;
    private final JTable bookingTable;
    private String startSlot = null;

    private String currentUserId;
    private String userRole;
    private final boolean isStaff; 

    // --- Search & Report components ---
    private JTextField txtSearchUser;
    private DefaultTableModel reportModel;
    private JLabel lblTotalBookings;

    private final String[] BOOKING_COLS = {"Booking ID", "User ID", "Facility ID", "Date", "Start Time", "End Time", "Status"};

    public BookingPanel(BookingManager bookingManager, FacilityManager facilityManager, String userID, String userRole) {
        this.bookingManager = bookingManager;
        this.facilityManager = facilityManager;
        this.currentUserId = userID;
        this.userRole = userRole;
        this.isStaff = "Staff".equals(userRole);

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Initialize table model
        bookingModel = new DefaultTableModel(BOOKING_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookingTable = new JTable(bookingModel);

        tabbedPane.addTab("🕒 Booking Management", createManagementTab());

        if (isStaff) {
            tabbedPane.addTab("📈 Popularity Report", createReportTab());
        }

        add(tabbedPane, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());

        // Search bar logic
        if (isStaff) {
            JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            txtSearchUser = new JTextField(15);
            JButton btnSearch = new JButton("🔍 Search Student ID");
            btnSearch.addActionListener(e -> performSearch());

            searchBar.add(new JLabel("Search Booking by Student ID:"));
            searchBar.add(txtSearchUser);
            searchBar.add(btnSearch);
            panel.add(searchBar, BorderLayout.NORTH);
        }

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        // Operation buttons
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
        panel.add(bp, BorderLayout.SOUTH);

        return panel;
    }

    // --- Tab 2: Facility report page ---
    private JPanel createReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTotalBookings = new JLabel("Total Bookings: 0");
        lblTotalBookings.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(lblTotalBookings, BorderLayout.NORTH);

        String[] cols = {"Facility ID", "Facility Name", "Times Booked", "Percentage"};
        reportModel = new DefaultTableModel(cols, 0);
        JTable reportTable = new JTable(reportModel);

        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        bookingModel.setRowCount(0);
        if (isStaff) {
            populateTable(bookingManager.getAllBookings());
        } else {
            populateTable(bookingManager.getBookingsByUser(currentUserId));
        }

        if (isStaff && reportModel != null) {
            updateReport();
        }
    }

    private void updateReport() {
        reportModel.setRowCount(0);
        SimpleList<Object[]> data = bookingManager.getPopularityReport();
        int total = 0;
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                Object[] row = data.get(i);
                reportModel.addRow(row);
                total += (int) row[2]; 
            }
        }
        lblTotalBookings.setText("Total Facility Reservations: " + total);
    }



    private void showBookingDialog() {
        // ---Step 1: Preview Selection Window ---
        JPanel pickPane = new JPanel(new GridLayout(0, 2, 5, 5));

        // Get all facilities
        SimpleList<Facility> facilities = facilityManager.searchByStatus("Available");
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

    // Note: Since the starting point of the grid was selected, the end time is the starting point + 30 minutes.
    private String calculateEndTime(String startTime) {
        String[] parts = startTime.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        m += 30;
        if (m >= 60) {
            h++;
            m = m - 60;
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
            updateGridColors(allButtons, slots);
        } else {
            String endSlot = calculateEndTime(clickedTime);

            // Logic A: Check if the clicked back was successful.
            if (clickedTime.compareTo(startSlot) < 0) {
                startSlot = clickedTime;
                updateGridColors(allButtons, slots);
                return;
            }

            if (!bookingManager.isDurationValid(startSlot, endSlot)) {
                JOptionPane.showMessageDialog(gridDialog,
                        "Booking limit exceeded! Maximum 2 hours (4 slots) per booking.",
                        "Time Limit", JOptionPane.WARNING_MESSAGE);

                startSlot = null;
                updateGridColors(allButtons, slots);
                return;
            }

            // Logic B: Check for conflicts in the middle (red square)
            if (bookingManager.isRangeAvailable(f.getId(), date, startSlot, endSlot)) {
                confirmFinalBooking(f, date, startSlot, endSlot, gridDialog);
                startSlot = null;
            } else {
                JOptionPane.showMessageDialog(gridDialog, "Conflict detected in selected range!");
                startSlot = null;
                updateGridColors(allButtons, slots);
            }
        }
    }

    private void updateGridColors(JButton[] buttons, SimpleList<SlotStatus> slots) {
        if (buttons == null || slots == null) {
            return;
        }

        for (int i = 0; i < slots.size(); i++) {
            if (i >= buttons.length || buttons[i] == null) {
                continue;
            }

            // Only available cells participate in the color-changing logic
            if (slots.get(i).isAvailable()) {
                String slotTime = slots.get(i).getTimeLabel();

                if (startSlot != null && slotTime.equals(startSlot)) {
                    buttons[i].setBackground(Color.YELLOW);
                } else {
                    buttons[i].setBackground(Color.GREEN);
                }
            }
        }
    }

    private void confirmFinalBooking(Facility f, String date, String start, String end, JDialog gridDialog) {
        String userId;
        if ("Staff".equals(userRole)) {
            userId = JOptionPane.showInputDialog(gridDialog, "Booking for Student (Enter ID):");
        } else {

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
        startSlot = null;
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

    private void populateTable(SimpleList<BookingRecord> list) {
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
