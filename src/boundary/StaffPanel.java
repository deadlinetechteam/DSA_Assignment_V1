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
import control.StaffManager;
import entitiy.Staff;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StaffPanel extends JPanel {

    private StaffManager staffManager;
    private DefaultTableModel staffModel;
    private JTable staffTable;

    // 对应 Staff 实体的字段：ID, Name, Password, Location, Department, Gender, Email
    // 参考自
    private final String[] STAFF_COLS = {"ID*", "Name*", "Password*", "Location", "Department", "Gender", "Email"};

    public StaffPanel(StaffManager staffManager) {
        setLayout(new BorderLayout());
        this.staffManager = staffManager;
        // --- 表格初始化 ---
        staffModel = new DefaultTableModel(STAFF_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        staffTable = new JTable(staffModel);
        staffTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 操作按钮面板 ---
        JPanel bp = new JPanel();
        JButton addB = new JButton("Add Staff");
        JButton upB = new JButton("Update Staff");
        JButton delB = new JButton("Delete Staff");

        addB.addActionListener(e -> showEntryDialog(null));

        // 更新逻辑
        upB.addActionListener(e -> {
            int r = staffTable.getSelectedRow();
            if (r != -1) {
                String id = (String) staffTable.getValueAt(r, 0);
                // 这里需要一个 getStaffById 的方法，确保 Manager 已经实现
                // showEntryDialog(staffManager.getStaff(id)); 
            } else {
                JOptionPane.showMessageDialog(this, "Select a staff member to update!");
            }
        });

        delB.addActionListener(e -> deleteLogic());

        bp.add(addB);
        bp.add(upB);
        bp.add(delB);

        add(new JScrollPane(staffTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        staffModel.setRowCount(0);
        // 这里假设你的 StaffManager 已经补充了 getAllStaffs() 方法
        // 它应该调用 StaffDAO.getAll()
        BPlusTree.SimpleList<Staff> sList = staffManager.getAllStaffs();

        if (sList != null) {
            for (int i = 0; i < sList.size(); i++) {
                Staff s = sList.get(i);
                staffModel.addRow(new Object[]{
                    s.getId(), s.getName(), s.getPassword(), s.getLocation(),
                    s.getDepartment(), s.getGender(), s.getEmail()
                });
            }
        }
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
            staffManager.deleteStaff(id); // 触发 B+ 树删除平衡逻辑
            refreshData();
        }
    }

    private void showEntryDialog(Staff exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[STAFF_COLS.length];

        for (int i = 0; i < STAFF_COLS.length; i++) {
            pane.add(new JLabel(STAFF_COLS[i] + ":"));
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            tfs[i] = new JTextField(val);
            if (i == 0 && exist != null) {
                tfs[i].setEditable(false);
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Staff Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (tfs[0].getText().trim().isEmpty() || tfs[1].getText().trim().isEmpty() || tfs[2].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID, Name and Password are mandatory!");
                return;
            }

            Staff s = new Staff(
                    tfs[0].getText().trim(), tfs[1].getText().trim(), tfs[2].getText().trim(),
                    tfs[3].getText().trim(), tfs[4].getText().trim(), tfs[5].getText().trim(),
                    tfs[6].getText().trim()
            );
//            staffManager.saveStaff(s); // 调用 Control 层持久化
            refreshData();
        }
    }

    private String getFieldValue(Staff s, int i) {
        switch (i) {
            case 0:
                return s.getId();
            case 1:
                return s.getName();
            case 2:
                return s.getPassword();
            case 3:
                return s.getLocation();
            case 4:
                return s.getDepartment();
            case 5:
                return s.getGender();
            case 6:
                return s.getEmail();
            default:
                return "";
        }
    }
}
