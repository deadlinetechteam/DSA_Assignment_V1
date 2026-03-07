/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */
import dao.StaffDAO;
import entitiy.Staff;
import adt.BPlusTree;

public class StaffManager {

    private StaffDAO staffDAO = new StaffDAO();

    public boolean authenticate(String id, String password) {
        Staff s = staffDAO.find(id);
        return s != null && s.getPassword().equals(password);
    }

    public void updateStaff(Staff s) {
        staffDAO.add(s);
    }

    public BPlusTree.SimpleList<Staff> getAllStudents() {
        return staffDAO.getAll(); // DAO 内部执行 tree.sort()
    }
}
