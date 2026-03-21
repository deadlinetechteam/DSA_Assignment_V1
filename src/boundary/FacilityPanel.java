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
import control.FacilityManager;
import entitiy.Facility;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FacilityPanel extends JPanel {

    private FacilityManager facilityManager;
    private final DefaultTableModel facilityModel;
    private JTable facilityTable;

    // --- 搜索组件 ---
    private final JComboBox<String> comboSearchType;
    private final JTextField txtSearch;
    private final JButton btnSearch;
    private final JButton btnReset;

    // 对应字段：ID, Name, Location, Venue, Type, Op Time, Capacity, Status
    private final String[] FACILITY_COLS = {"ID*", "Name*", "Location", "Venue", "Type", "Op Time*", "Capacity", "Status"};

    public FacilityPanel(FacilityManager facilityManager, String userRole) {
        setLayout(new BorderLayout());
        this.facilityManager = facilityManager;

        // --- 1. 表格初始化 ---
        facilityModel = new DefaultTableModel(FACILITY_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        facilityTable = new JTable(facilityModel);
        facilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 2. 初始化搜索栏 (顶部) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] searchOptions = {"Name", "ID", "Type", "Status"};
        comboSearchType = new JComboBox<>(searchOptions);
        txtSearch = new JTextField(15);
        btnSearch = new JButton("🔍 Search");
        btnReset = new JButton("🔄 Reset");

        searchPanel.add(new JLabel("Search By:"));
        searchPanel.add(comboSearchType);
        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        // --- 3. 绑定搜索和重置事件 ---
        btnSearch.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch()); // 回车搜索
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        // --- 4. 操作按钮面板 (底部) ---
        if ("Staff".equals(userRole)) {
            JPanel bp = new JPanel();
            JButton addB = new JButton("Add Facility");
            JButton upB = new JButton("Update Facility");
            JButton delB = new JButton("Delete Facility");

            addB.addActionListener(e -> showEntryDialog(null));
            upB.addActionListener(e -> {
                int r = facilityTable.getSelectedRow();
                if (r != -1) {
                    String id = (String) facilityTable.getValueAt(r, 0);
                    showEntryDialog(facilityManager.readFacility(id));
                } else {
                    JOptionPane.showMessageDialog(this, "Select a facility to update!");
                }
            });
            delB.addActionListener(e -> deleteLogic());

            bp.add(addB);
            bp.add(upB);
            bp.add(delB);
            add(bp, BorderLayout.SOUTH);
        }
        // --- 5. 组装布局 ---
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(facilityTable), BorderLayout.CENTER);

        refreshData();
    }

    // 统一填充表格的方法
    private void populateTable(SimpleList<Facility> list) {
        facilityModel.setRowCount(0);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Facility f = list.get(i);
            facilityModel.addRow(new Object[]{
                f.getId(), f.getName(), f.getLocation(), f.getVenue(),
                f.getVenueType(), f.getOperationTime(), f.getCapacity(), f.getStatus()
            });
        }
    }

    public void refreshData() {
        populateTable(facilityManager.getAllFacilities());
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String type = (String) comboSearchType.getSelectedItem();

        if (keyword.isEmpty()) {
            refreshData();
            return;
        }

        SimpleList<Facility> results;
        switch (type) {
            case "ID" ->
                results = facilityManager.searchByID(keyword);
            case "Name" ->
                results = facilityManager.searchByName(keyword);
            case "Type" ->
                results = facilityManager.searchByVenueType(keyword);
            case "Status" ->
                results = facilityManager.searchByStatus(keyword);
            default ->
                results = facilityManager.getAllFacilities();
        }
        populateTable(results);
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
            facilityManager.deleteFacility(id);
            refreshData();
        }
    }

    private void showEntryDialog(Facility exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[FACILITY_COLS.length];

        for (int i = 0; i < FACILITY_COLS.length; i++) {
            pane.add(new JLabel(FACILITY_COLS[i] + ":"));

            // 自动 ID 逻辑
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            if (exist == null && i == 0) {
                val = facilityManager.generateNextId();
            }
            if (exist == null && i == 7) {
                val = "Available";
            }
            tfs[i] = new JTextField(val);

            // 锁定 ID 字段
            if (i == 0) {
                tfs[i].setEditable(false);
                tfs[i].setBackground(new Color(235, 235, 235));
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Facility Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                // 校验必填项
                if (tfs[1].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Facility Name is mandatory!");
                    return;
                }
                if (tfs[5].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Operation time is mandatory!");
                    return;
                }

                Facility f = new Facility(
                        tfs[0].getText().trim(), tfs[1].getText().trim(), tfs[2].getText().trim(),
                        tfs[3].getText().trim(), tfs[4].getText().trim(), tfs[5].getText().trim(),
                        Integer.parseInt(tfs[6].getText().trim()), tfs[7].getText().trim()
                );

                if (exist != null) {
                    facilityManager.updateFacility(f); // 调用全量更新逻辑
                    JOptionPane.showMessageDialog(this, "Facility updated successfully!");
                } else {
                    facilityManager.createFacility(f);
                    JOptionPane.showMessageDialog(this, "New facility added!");
                }
                refreshData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacity must be a valid number!");
            }
        }
    }

    private String getFieldValue(Facility f, int i) {
        return switch (i) {
            case 0 ->
                f.getId();
            case 1 ->
                f.getName();
            case 2 ->
                f.getLocation();
            case 3 ->
                f.getVenue();
            case 4 ->
                f.getVenueType();
            case 5 ->
                f.getOperationTime();
            case 6 ->
                String.valueOf(f.getCapacity());
            case 7 ->
                f.getStatus();
            default ->
                "";
        };
    }
}
