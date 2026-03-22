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
import control.StaffManager;
import entitiy.Staff;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StaffPanel extends JPanel {

    private StaffManager staffManager;
    private final DefaultTableModel staffModel;
    private JTable staffTable;

    // 搜索组件
    private JComboBox<String> comboSearchType;
    private final JTextField txtSearch;
    private final JButton btnSearch;
    private final JButton btnReset;

    private final String[] STAFF_COLS = {"ID*", "Name*", "Password*", "Location", "Department", "Gender", "Email"};

    public StaffPanel(StaffManager staffManager) {
        setLayout(new BorderLayout());
        this.staffManager = staffManager;

        // --- 1. Table initialization ---
        staffModel = new DefaultTableModel(STAFF_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        staffTable = new JTable(staffModel);
        staffTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 2. Initialize the search bar (top) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] searchOptions = {"Name", "ID", "Gender"};
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

        // --- 3. Binding search and reset events ---
        btnSearch.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch()); 
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        // --- 4. Operation button panel (bottom) ---
        JPanel bp = new JPanel();
        JButton addB = new JButton("Add Staff");
        JButton upB = new JButton("Update Staff");
        JButton delB = new JButton("Delete Staff");

        addB.addActionListener(e -> showEntryDialog(null));
        upB.addActionListener(e -> {
            int r = staffTable.getSelectedRow();
            if (r != -1) {
                String id = (String) staffTable.getValueAt(r, 0);
                showEntryDialog(staffManager.readStaff(id));
            } else {
                JOptionPane.showMessageDialog(this, "Select a staff member to update!");
            }
        });
        delB.addActionListener(e -> deleteLogic());

        bp.add(addB);
        bp.add(upB);
        bp.add(delB);

        // --- 5. Assembly layout ---
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(staffTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        refreshData();
    }

    // Populate the table with SimpleList.
    private void populateTable(SimpleList<Staff> list) {
        staffModel.setRowCount(0);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Staff s = list.get(i);
            staffModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getPassword(), s.getLocation(),
                s.getDepartment(), s.getGender(), s.getEmail()
            });
        }
    }

    public void refreshData() {
        populateTable(staffManager.getAllStaffs());
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String type = (String) comboSearchType.getSelectedItem();

        if (keyword.isEmpty()) {
            refreshData();
            return;
        }

        SimpleList<Staff> results;
        switch (type) {
            case "ID" ->
                results = staffManager.searchByID(keyword);
            case "Name" ->
                results = staffManager.searchByName(keyword);
            case "Gender" ->
                results = staffManager.searchByGender(keyword);
            default ->
                results = staffManager.getAllStaffs();
        }
        populateTable(results);
    }

    private void deleteLogic() {
        int r = staffTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a staff member!");
            return;
        }

        String id = (String) staffTable.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete Staff " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            staffManager.deleteStaff(id);
            refreshData();
        }
    }

    private void showEntryDialog(Staff exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[STAFF_COLS.length];

        for (int i = 0; i < STAFF_COLS.length; i++) {
            pane.add(new JLabel(STAFF_COLS[i] + ":"));

            // If it's a new creation, the ID is automatically generated; if it's an edit, the old value is read.
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            if (exist == null && i == 0) {
                val = staffManager.generateNextId(); 
            }

            tfs[i] = new JTextField(val);

            // Lock the ID field to prevent users from editing it.
            if (i == 0) {
                tfs[i].setEditable(false);
                tfs[i].setBackground(new Color(235, 235, 235)); 
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Staff Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            // Validation required fields
            if (tfs[1].getText().trim().isEmpty() || tfs[2].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Password are mandatory!");
                return;
            }

            Staff s = new Staff(
                    tfs[0].getText().trim(), tfs[1].getText().trim(), tfs[2].getText().trim(),
                    tfs[3].getText().trim(), tfs[4].getText().trim(), tfs[5].getText().trim(),
                    tfs[6].getText().trim()
            );

            if (exist != null) {
                staffManager.updateStaff(s);
                JOptionPane.showMessageDialog(this, "Staff updated!");
            } else {
                staffManager.createStaff(s);
                JOptionPane.showMessageDialog(this, "Staff added!");
            }
            refreshData();
        }
    }

    private String getFieldValue(Staff s, int i) {
        return switch (i) {
            case 0 ->
                s.getId();
            case 1 ->
                s.getName();
            case 2 ->
                s.getPassword();
            case 3 ->
                s.getLocation();
            case 4 ->
                s.getDepartment();
            case 5 ->
                s.getGender();
            case 6 ->
                s.getEmail();
            default ->
                "";
        };
    }
}
