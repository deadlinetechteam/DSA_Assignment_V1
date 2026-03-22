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
import control.StudentManager;
import entitiy.Student;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentPanel extends JPanel {

    private StudentManager studentManager;
    private final DefaultTableModel studentModel;
    private JTable studentTable;

    // --- Search component ---
    private final JComboBox<String> comboSearchType;
    private final JTextField txtSearch;
    private final JButton btnSearch;
    private final JButton btnReset;

    private final String[] STUDENT_COLS = {"ID*", "Name*", "Password*", "Gender", "MykadNO", "Email", "Programme", "Address", "ContactNo"};

    public StudentPanel(StudentManager studentManager) {
        setLayout(new BorderLayout());
        this.studentManager = studentManager;

        // --- 1. Table initialization ---
        studentModel = new DefaultTableModel(STUDENT_COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        studentTable = new JTable(studentModel);
        studentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // --- 2. Initialize the search bar (Top) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] searchOptions = {"Name", "ID", "Programme"};
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

        // --- 3. Bind search and reset events ---
        btnSearch.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch()); 
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        // --- 4. Operation button panel (bottom) ---
        JPanel bp = new JPanel();
        JButton addB = new JButton("Add Student");
        JButton upB = new JButton("Update Student");
        JButton delB = new JButton("Delete Student");

        addB.addActionListener(e -> showEntryDialog(null));
        upB.addActionListener(e -> {
            int r = studentTable.getSelectedRow();
            if (r != -1) {
                String id = (String) studentTable.getValueAt(r, 0);
                showEntryDialog(studentManager.readStudent(id));
            } else {
                JOptionPane.showMessageDialog(this, "Select a student to update!");
            }
        });
        delB.addActionListener(e -> deleteLogic());

        bp.add(addB);
        bp.add(upB);
        bp.add(delB);

        // --- 5. Assembly layout ---
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(studentTable), BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);

        refreshData();
    }

    private void populateTable(SimpleList<Student> list) {
        studentModel.setRowCount(0);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Student s = list.get(i);
            studentModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getPassword(), s.getGender(),
                s.getMykadNO(), s.getEmail(), s.getProgramme(),
                s.getAddress(), s.getContactNo()
            });
        }
    }

    public void refreshData() {
        populateTable(studentManager.getAllStudents());
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String type = (String) comboSearchType.getSelectedItem();

        if (keyword.isEmpty()) {
            refreshData();
            return;
        }

        SimpleList<Student> results;
        switch (type) {
            case "ID" ->
                results = studentManager.searchByID(keyword);
            case "Name" ->
                results = studentManager.searchByName(keyword);
            case "Programme" ->
                results = studentManager.searchByProgramme(keyword);
            default ->
                results = studentManager.getAllStudents();
        }
        populateTable(results);
    }

    private void deleteLogic() {
        int r = studentTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a student!");
            return;
        }

        String id = (String) studentTable.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete Student " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            studentManager.deleteStudent(id);
            refreshData();
        }
    }

    private void showEntryDialog(Student exist) {
        JPanel pane = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] tfs = new JTextField[STUDENT_COLS.length];

        for (int i = 0; i < STUDENT_COLS.length; i++) {
            pane.add(new JLabel(STUDENT_COLS[i] + ":"));

            // automatic ID logic
            String val = (exist == null) ? "" : getFieldValue(exist, i);
            if (exist == null && i == 0) {
                val = studentManager.generateNextId(); 
            }

            tfs[i] = new JTextField(val);

            // Lock ID field
            if (i == 0) {
                tfs[i].setEditable(false);
                tfs[i].setBackground(new Color(235, 235, 235));
            }
            pane.add(tfs[i]);
        }

        if (JOptionPane.showConfirmDialog(null, pane, "Student Entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            // Validation required fields (ID, Name, Password)
            if (tfs[1].getText().trim().isEmpty() || tfs[2].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Password are mandatory!");
                return;
            }

            Student s = new Student(
                    tfs[0].getText().trim(), tfs[1].getText().trim(), tfs[2].getText().trim(),
                    tfs[3].getText().trim(), tfs[4].getText().trim(), tfs[5].getText().trim(),
                    tfs[6].getText().trim(), tfs[7].getText().trim(), tfs[8].getText().trim()
            );

            if (exist != null) {
                studentManager.updateStudent(s);
                JOptionPane.showMessageDialog(this, "Student updated successfully!");
            } else {
                studentManager.createStudent(s);
                JOptionPane.showMessageDialog(this, "New student added!");
            }
            refreshData();
        }
    }

    private String getFieldValue(Student s, int i) {
        return switch (i) {
            case 0 ->
                s.getId();
            case 1 ->
                s.getName();
            case 2 ->
                s.getPassword();
            case 3 ->
                s.getGender();
            case 4 ->
                s.getMykadNO();
            case 5 ->
                s.getEmail();
            case 6 ->
                s.getProgramme();
            case 7 ->
                s.getAddress();
            case 8 ->
                s.getContactNo();
            default ->
                "";
        };
    }
}
