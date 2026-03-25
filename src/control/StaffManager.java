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
import adt.BPlusTree.SimpleList;
import utility.IndexHelper;

public class StaffManager {

    private final BPlusTree<String, Staff> mainTree;
    private BPlusTree<String, SimpleList<String>> nameIndex;
    private int nextIdNum;

    public StaffManager() {
        String path = "staffs.bin";
        BPlusTree<String, Staff> loadedTree = BPlusTree.load(path);

        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }
        rebuildIndex();
        this.nextIdNum = calculateInitialCounter();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                System.out.println("[Auto-Save] Saving staff data to disk...");
                mainTree.commit();
            }
        }));
    }

    public void createStaff(Staff newStaff) {
        mainTree.create(newStaff.getId(), newStaff);
        IndexHelper.addToIndex(nameIndex, newStaff.getName(), newStaff.getId());
    }

    public Staff readStaff(String id) {
        return mainTree.read(id);
    }

    public void updateStaff(Staff updatedStaff) {
        Staff oldStaff = mainTree.read(updatedStaff.getId());
        if (oldStaff != null) {
            if (!oldStaff.getName().equals(updatedStaff.getName())) {
                SimpleList<String> oldIds = nameIndex.read(oldStaff.getName());
                if (oldIds != null) {
                    oldIds.remove(oldStaff.getId());
                    if (oldIds.size() == 0) {
                        nameIndex.delete(oldStaff.getName());
                    }
                }
                IndexHelper.addToIndex(nameIndex, updatedStaff.getName(), updatedStaff.getId());
            }
        }
        mainTree.update(updatedStaff.getId(), updatedStaff);
    }

    public void deleteStaff(String id) {
        Staff s = mainTree.read(id);
        if (s != null) {

            IndexHelper.removeFromIndex(nameIndex, s.getName(), id);
            mainTree.delete(id);
        }
    }

    public boolean authenticate(String id, String password) {
        Staff s = mainTree.read(id);
        return s != null && s.getPassword().equals(password);
    }

    public BPlusTree.SimpleList<Staff> getAllStaffs() {
        return mainTree.sort();
    }

    private int calculateInitialCounter() {
        SimpleList<Staff> all = mainTree.sort();
        int max = 0;
        for (int i = 0; i < all.size(); i++) {
            try {
                int currentNum = Integer.parseInt(all.get(i).getId().substring(1));
                if (currentNum > max) {
                    max = currentNum;
                }
            } catch (NumberFormatException e) {
            }
        }
        return max;
    }

    public String generateNextId() {
        nextIdNum++;
        return String.format("S%03d", nextIdNum);
    }

    private void rebuildIndex() {
        this.nameIndex = new BPlusTree<>(10);
        SimpleList<Staff> allStaffs = mainTree.sort();

        for (int i = 0; i < allStaffs.size(); i++) {
            Staff s = allStaffs.get(i);
            IndexHelper.addToIndex(nameIndex, s.getName(), s.getId());
        }
    }

    public SimpleList<Staff> searchByID(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStaffs();
        }
        SimpleList<Staff> results = mainTree.searchRange(keyword, keyword + "\uffff");
        return results;
    }

    public SimpleList<Staff> searchByName(String keyword) {
        SimpleList<Staff> results = new SimpleList<>();
        SimpleList<SimpleList<String>> idLists = nameIndex.searchRange(keyword, keyword + "\uffff");
        for (int i = 0; i < idLists.size(); i++) {
            SimpleList<String> ids = idLists.get(i);
            for (int j = 0; j < ids.size(); j++) {
                Staff s = mainTree.read(ids.get(j));
                if (s != null) {
                    results.add(s);
                }
            }
        }
        return results;
    }

    public SimpleList<Staff> searchByGender(String gender) {
        SimpleList<Staff> results = new SimpleList<>();
        SimpleList<Staff> allStaff = mainTree.sort();
        for (int i = 0; i < allStaff.size(); i++) {
            if (allStaff.get(i).getGender().equalsIgnoreCase(gender)) {
                results.add(allStaff.get(i));
            }
        }
        return results;
    }

}
