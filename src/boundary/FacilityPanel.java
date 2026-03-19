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
import control.FacilityManager;
import entitiy.Facility;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FacilityPanel extends JPanel {

    private FacilityManager facilityManager = new FacilityManager();
    private DefaultTableModel facilityModel;
    private JTable facilityTable;

    // 对应 Facility 实体的字段：ID, Name, Location, Venue, VenueType, Time, Capacity, Status
    private final String[] FACILITY_COLS = {"ID*", "Name*", "Location", "Venue", "Type", "Op Time", "Capacity", "Status"};

    public FacilityPanel() {
        setLayout(new BorderLayout());

        // --- 表格初始化 ---
        facilityModel = new DefaultTableModel(FACILITY_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        facilityTable = new JTable(facilityModel);
        facilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 操作按钮面板 ---
        JPanel bp = new JPanel();
        JButton addB = new JButton("Add Facility");
        JButton upB = new JButton("Update Status");
        JButton delB = new JButton("Delete Facility");

        addB.addActionListener(e -> showEntryDialog(null));

        // 更新状态逻辑：例如手动将设施设为维护中 (Maintenance)
        upB.addActionListener(e -> {
            int r = facilityTable.getSelectedRow();
            if (r != -1) {
                String id = (String) facilityTable.getValueAt(r, 0);
                String newStatus = JOptionPane.showInputDialog(this, "Enter new status (Available/Maintenance):");
                if (newStatus != null && !newStatus.isEmpty()) {
                    Facility f = facilityManager.readFacility(id);//.get(r); // 简化获取方式
                    f.setStatus(newStatus);
                    facilityManager.addFacility(f);
                    refreshData();
                }
            }
        });

        delB.addActionListener(e -> deleteLogic());

        bp.add(addB);
        bp.add(upB);
        bp.add(delB);

        add(new JScrollPane(facilityTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        facilityModel.setRowCount(0);
        // 从 Manager 获取 B+ 树中的所有设施
        BPlusTree.SimpleList<Facility> fList = facilityManager.getAllFacilities();

        for (int i = 0; i < fList.size(); i++) {
            Facility f = fList.get(i);
            facilityModel.addRow(new Object[]{
                f.getId(), f.getName(), f.getLocation(), f.getVenue(),
                f.getVenueType(), f.getOperationTime(), f.getCapacity(), f.getStatus()
            });
        }
    }

    private void deleteLogic() {
        int r = facilityTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a facility!");
            return;
        }

        String id = (String) facilityTable.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete Facility " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            facilityManager.deleteFacility(id); // 触发 B+ 树删除平衡逻辑
            refreshData();
        }
    }

    private void showEntryDialog(Facility exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[FACILITY_COLS.length];

        for (int i = 0; i < FACILITY_COLS.length; i++) {
            pane.add(new JLabel(FACILITY_COLS[i] + ":"));
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            tfs[i] = new JTextField(val);
            if (i == 0 && exist != null) {
                tfs[i].setEditable(false);
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Facility Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Facility f = new Facility(
                        tfs[0].getText().trim(), tfs[1].getText().trim(), tfs[2].getText().trim(),
                        tfs[3].getText().trim(), tfs[4].getText().trim(), tfs[5].getText().trim(),
                        Integer.parseInt(tfs[6].getText().trim()), tfs[7].getText().trim()
                );
                facilityManager.addFacility(f); // 持久化
                refreshData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacity must be a number!");
            }
        }
    }

    private String getFieldValue(Facility f, int i) {
        switch (i) {
            case 0:
                return f.getId();
            case 1:
                return f.getName();
            case 2:
                return f.getLocation();
            case 3:
                return f.getVenue();
            case 4:
                return f.getVenueType();
            case 5:
                return f.getOperationTime();
            case 6:
                return String.valueOf(f.getCapacity());
            case 7:
                return f.getStatus();
            default:
                return "";
        }
    }
}
