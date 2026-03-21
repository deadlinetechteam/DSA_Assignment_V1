/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */
import entitiy.Facility;
import adt.BPlusTree;
import adt.BPlusTree.SimpleList;

public class FacilityManager {

    private final BPlusTree<String, Facility> mainTree;
    private BPlusTree<String, SimpleList<String>> nameIndex;
    private int nextIdNum;

    public FacilityManager() {
        String path = "facilities.bin";
        BPlusTree<String, Facility> loadedTree = BPlusTree.load(path);

        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        this.nextIdNum = calculateInitialCounter();
        rebuildIndex();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                System.out.println("[Auto-Save] Saving facility data to disk...");
                mainTree.commit();
            }
        }));
    }

    public BPlusTree<String, Facility> getTree() {
        return mainTree;
    }

    public void createFacility(Facility newFacility) {
        mainTree.create(newFacility.getId(), newFacility);
        addNameToIndex(newFacility.getName(), newFacility.getId());
    }

    public Facility readFacility(String id) {
        return mainTree.read(id);
    }

    public void updateFacility(Facility updatedFacility) {
        Facility oldFacility = mainTree.read(updatedFacility.getId());
        if (oldFacility != null) {
            if (!oldFacility.getName().equals(updatedFacility.getName())) {
                removeNameFromIndex(oldFacility.getName(), oldFacility.getId());
                addNameToIndex(updatedFacility.getName(), updatedFacility.getId());
            }
        }
        mainTree.update(updatedFacility.getId(), updatedFacility);
    }

    public void deleteFacility(String id) {
        Facility f = mainTree.read(id);
        if (f != null) {
            removeNameFromIndex(f.getName(), id);
            mainTree.delete(id);
        }
    }

    public BPlusTree.SimpleList<Facility> getAllFacilities() {
        return mainTree.sort();
    }

    private int calculateInitialCounter() {
        SimpleList<Facility> all = mainTree.sort();
        int max = 0;
        for (int i = 0; i < all.size(); i++) {
            try {
                int currentNum = Integer.parseInt(all.get(i).getId().substring(1));
                if (currentNum > max) {
                    max = currentNum;
                }
            } catch (Exception e) {
            }
        }
        return max;
    }

    public String generateNextId() {
        nextIdNum++;
        return String.format("F%03d", nextIdNum);
    }

    public SimpleList<Facility> searchByID(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllFacilities();
        }
        return mainTree.searchRange(keyword, keyword + "\uffff");
    }

    public SimpleList<Facility> searchByName(String keyword) {
        SimpleList<Facility> results = new SimpleList<>();
        SimpleList<SimpleList<String>> idLists = nameIndex.searchRange(keyword, keyword + "\uffff");

        for (int i = 0; i < idLists.size(); i++) {
            SimpleList<String> ids = idLists.get(i);
            for (int j = 0; j < ids.size(); j++) {
                Facility f = mainTree.read(ids.get(j));
                if (f != null) {
                    results.add(f);
                }
            }
        }
        return results;
    }

    public SimpleList<Facility> searchByVenueType(String type) {
        SimpleList<Facility> results = new SimpleList<>();
        SimpleList<Facility> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getVenueType().toLowerCase().contains(type.toLowerCase())) {
                results.add(all.get(i));
            }
        }
        return results;
    }

    public SimpleList<Facility> searchByStatus(String status) {
        SimpleList<Facility> results = new SimpleList<>();
        SimpleList<Facility> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getStatus().equalsIgnoreCase(status)) {
                results.add(all.get(i));
            }
        }
        return results;
    }

    private void rebuildIndex() {
        this.nameIndex = new BPlusTree<>(10);
        SimpleList<Facility> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            Facility f = all.get(i);
            addNameToIndex(f.getName(), f.getId());
        }
    }

    private void addNameToIndex(String name, String id) {
        SimpleList<String> ids = nameIndex.read(name);
        if (ids == null) {
            ids = new SimpleList<>();
            nameIndex.create(name, ids);
        }
        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    private void removeNameFromIndex(String name, String id) {
        SimpleList<String> ids = nameIndex.read(name);
        if (ids != null) {
            ids.remove(id);
            if (ids.size() == 0) {
                nameIndex.delete(name);
            }
        }
    }
}
