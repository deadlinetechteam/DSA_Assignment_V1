/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */
import entitiy.Staff;
import adt.BPlusTree;

public class StaffManager {

    private final BPlusTree<String, Staff> mainTree;

    public StaffManager() {
        String path = "staffs.bin";
        BPlusTree<String, Staff> loadedTree = BPlusTree.load(path);

        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                System.out.println("[Auto-Save] Saving staff data to disk...");
                mainTree.commit();
            }
        }));

    }

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
        mainTree.delete(s);
    }

    public boolean authenticate(String id, String password) {
        Staff s = mainTree.read(id);
        return s != null && s.getPassword().equals(password);
    }

    public BPlusTree.SimpleList<Staff> getAllStaffs() {
        return mainTree.sort();
    }
}
