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
import control.StudentManager;
import entitiy.Student;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentPanel extends JPanel {

    private StudentManager studentManager;
    private DefaultTableModel studentModel;
    private JTable studentTable;

    // 对应你 MainPage 中的列定义
    private final String[] STUDENT_COLS = {"ID*", "Name*", "Password*", "Gender", "MykadNO", "Email", "Programme", "Address", "ContactNo"};

    public StudentPanel(StudentManager studentManager) {
        setLayout(new BorderLayout());
        this.studentManager = studentManager;
        // --- 表格初始化 ---
        studentModel = new DefaultTableModel(STUDENT_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        studentTable = new JTable(studentModel);
        studentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 操作按钮 ---
        JPanel bp = new JPanel();
        JButton addB = new JButton("Add Student");
        JButton upB = new JButton("Update Student");
        JButton delB = new JButton("Delete Student");

        addB.addActionListener(e -> showEntryDialog(null));
        delB.addActionListener(e -> deleteLogic());

        bp.add(addB);
        bp.add(upB);
        bp.add(delB);

        add(new JScrollPane(studentTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        studentModel.setRowCount(0);
        // 通过 Manager 获取 B+ 树中的所有学生数据
        BPlusTree.SimpleList<Student> sList = studentManager.getAllStudents();

        for (int i = 0; i < sList.size(); i++) {
            Student s = sList.get(i);
            studentModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getPassword(), s.getGender(),
                s.getMykadNO(), s.getEmail(), s.getProgramme(),
                s.getAddress(), s.getContactNo()
            });
        }
    }

    private void deleteLogic() {
        int r = studentTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a student!");
            return;
        }

        String id = (String) studentTable.getValueAt(r, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
//            studentManager.deleteStudent(id); // 触发 B+ 树删除逻辑
            refreshData();
        }
    }

    private void showEntryDialog(Student exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[STUDENT_COLS.length];

        for (int i = 0; i < STUDENT_COLS.length; i++) {
            pane.add(new JLabel(STUDENT_COLS[i] + ":"));
            String val = "";
            if (exist != null) {
                val = getFieldValue(exist, i);
            }
            tfs[i] = new JTextField(val);
            if (i == 0 && exist != null) {
                tfs[i].setEditable(false);
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Student Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (tfs[0].getText().trim().isEmpty() || tfs[1].getText().trim().isEmpty() || tfs[2].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID, Name and Password are mandatory!");
                return;
            }

            Student s = new Student(
                    tfs[0].getText().trim(), tfs[1].getText().trim(), tfs[2].getText().trim(),
                    tfs[3].getText().trim(), tfs[4].getText().trim(), tfs[5].getText().trim(),
                    tfs[6].getText().trim(), tfs[7].getText().trim(), tfs[8].getText().trim()
            );

            studentManager.saveStudent(s); // 调用 Control 层进行保存（含文件持久化）
            refreshData();
        }
    }

    private String getFieldValue(Student s, int i) {
        switch (i) {
            case 0:
                return s.getId();
            case 1:
                return s.getName();
            case 2:
                return s.getPassword();
            case 3:
                return s.getGender();
            case 4:
                return s.getMykadNO();
            case 5:
                return s.getEmail();
            case 6:
                return s.getProgramme();
            case 7:
                return s.getAddress();
            case 8:
                return s.getContactNo();
            default:
                return "";
        }
    }

}
