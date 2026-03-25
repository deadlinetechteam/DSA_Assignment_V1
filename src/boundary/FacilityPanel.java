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

    // --- Search component ---
    private final JComboBox<String> comboSearchType;
    private final JTextField txtSearch;
    private final JButton btnSearch;
    private final JButton btnReset;

    // --- Report component ---
    private DefaultTableModel reportModel;
    private JLabel lblTotalFacilities;
    private final boolean isStaff;

    private final String[] FACILITY_COLS = {"ID*", "Name*", "Location", "Venue", "Type", "Op Time*(HH:mm)", "Capacity*", "Status"};

    public FacilityPanel(FacilityManager facilityManager, String userRole) {
        this.facilityManager = facilityManager;
        this.isStaff = "Staff".equalsIgnoreCase(userRole);
        setLayout(new BorderLayout());

        // 1. Initialize search component
        String[] searchOptions = {"Name", "ID", "Type", "Status"};
        comboSearchType = new JComboBox<>(searchOptions);
        txtSearch = new JTextField(15);
        btnSearch = new JButton("🔍 Search");
        btnReset = new JButton("🔄 Reset");

        // 2. Initialize table
        facilityModel = new DefaultTableModel(FACILITY_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        facilityTable = new JTable(facilityModel);
        facilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // 3. Building a Tab Container
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("🏟️ Facility Management", createManagementTab(userRole));

        if (isStaff) {
            tabbedPane.addTab("📊 Venue Type Report", createReportTab());
        }

        add(tabbedPane, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createManagementTab(String userRole) {
        JPanel panel = new JPanel(new BorderLayout());

        // Top search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search By:"));
        searchPanel.add(comboSearchType);
        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        btnSearch.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(facilityTable), BorderLayout.CENTER);

        // Staff button at the bottom
        if (isStaff) {
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
                    JOptionPane.showMessageDialog(this, "Select a facility!");
                }
            });
            delB.addActionListener(e -> deleteLogic());

            bp.add(addB);
            bp.add(upB);
            bp.add(delB);
            panel.add(bp, BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel createReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTotalFacilities = new JLabel("Total Facilities: 0");
        lblTotalFacilities.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(lblTotalFacilities, BorderLayout.NORTH);

        String[] cols = {"Venue Type", "Count", "Percentage"};
        reportModel = new DefaultTableModel(cols, 0);
        JTable reportTable = new JTable(reportModel);

        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        populateTable(facilityManager.getAllFacilities());
        if (isStaff && reportModel != null) {
            updateReport();
        }
    }

    private void updateReport() {
        reportModel.setRowCount(0);
        SimpleList<Object[]> data = facilityManager.getVenueTypeReport();
        int total = 0;
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            reportModel.addRow(row);
            total += (int) row[1];
        }
        lblTotalFacilities.setText("Total Campus Facilities: " + total);
    }

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

            // Automatic ID logic
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            if (exist == null && i == 0) {
                val = facilityManager.generateNextId();
            }
            if (exist == null && i == 7) {
                val = "Available";
            }
            tfs[i] = new JTextField(val);

            // Lock ID field
            if (i == 0) {
                tfs[i].setEditable(false);
                tfs[i].setBackground(new Color(235, 235, 235));
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Facility Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                // Validation required fields
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
                    facilityManager.updateFacility(f);
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
