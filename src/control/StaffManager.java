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

    private final BPlusTree<String, Staff> mainTree = new BPlusTree<>(4);
    private StaffDAO staffDAO = new StaffDAO();

    public void createStaff(Staff newStaff) {
        mainTree.create(newStaff.getId(), newStaff);
    }

    public Staff readStaff(String id) {
        return mainTree.read(id);
    }

    public void updateStaff(Staff s) {
        mainTree.update(s.getId(), s);
    }

    public void deleteStaff(String s) {
        mainTree.delete(s); // DAO 内部执行 tree.delete (触发 B+ 树平衡)
    }

    public void saveStaff(Staff s) {
        mainTree.create(s.getId(), s); // DAO 内部会执行 tree.create 并 saveToFile
    }

    public boolean authenticate(String id, String password) {
        Staff s = mainTree.read(id);
        return s != null && s.getPassword().equals(password);
    }

    public BPlusTree.SimpleList<Staff> getAllStaffs() {
        return mainTree.sort(); // DAO 内部执行 tree.sort()
    }
}
