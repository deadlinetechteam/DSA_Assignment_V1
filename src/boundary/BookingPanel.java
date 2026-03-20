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
import control.BookingManager;
import entitiy.BookingRecord;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

public class BookingPanel extends JPanel {

    private BookingManager bookingManager;
    private DefaultTableModel bookingModel;
    private JTable bookingTable;

    // 对应 BookingRecord 实体的字段
    private final String[] BOOKING_COLS = {"Booking ID", "User ID", "Facility ID", "Date", "Start Time", "End Time", "Status"};

    public BookingPanel(BookingManager bookingManager) {
        setLayout(new BorderLayout());
        this.bookingManager = bookingManager;
        // --- 表格初始化 ---
        bookingModel = new DefaultTableModel(BOOKING_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookingTable = new JTable(bookingModel);

        // --- 操作按钮 ---
        JPanel bp = new JPanel();
        JButton btnNewBooking = new JButton("New Booking");
        JButton btnCancel = new JButton("Cancel Booking");
        JButton btnRefresh = new JButton("Refresh");

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
        // 从 BookingManager 获取所有预约记录
        BPlusTree.SimpleList<BookingRecord> list = bookingManager.getAllBookings();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                BookingRecord r = list.get(i);
                bookingModel.addRow(new Object[]{
                    r.getId(), r.getUserId(), r.getFacilityId(),
                    r.getBookingDate(), r.getStartTime(), r.getEndTime(), r.getStatus()
                });
            }
        }
    }

    private void showBookingDialog() {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField txtUserId = new JTextField();
        JTextField txtFacilityId = new JTextField();
        JTextField txtDate = new JTextField("2026-03-07");
        JTextField txtStart = new JTextField("10:00");
        JTextField txtEnd = new JTextField("12:00");

        pane.add(new JLabel("User ID:"));
        pane.add(txtUserId);
        pane.add(new JLabel("Facility ID:"));
        pane.add(txtFacilityId);
        pane.add(new JLabel("Date:"));
        pane.add(txtDate);
        pane.add(new JLabel("Start Time:"));
        pane.add(txtStart);
        pane.add(new JLabel("End Time:"));
        pane.add(txtEnd);

        int result = JOptionPane.showConfirmDialog(null, pane, "New Facility Booking", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // 调用 BookingManager 处理预约逻辑
            boolean success = bookingManager.makeBooking(
                    txtUserId.getText().trim(),
                    txtFacilityId.getText().trim(),
                    txtDate.getText().trim(),
                    txtStart.getText().trim(),
                    txtEnd.getText().trim()
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Booking Successful!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Booking Failed: Facility not available.");
            }
        }
    }

    private void cancelLogic() {
        int r = bookingTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel!");
            return;
        }

        String bId = (String) bookingTable.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel Booking " + bId + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 调用 BookingManager 取消预约并触发 B+ 树删除
            bookingManager.cancelBooking(bId);
            refreshData();
        }
    }
}
