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

public class FacilityManager {

    private final BPlusTree<String, Facility> mainTree;

    public FacilityManager() {
        String path = "facilities.bin";
        BPlusTree<String, Facility> loadedTree = BPlusTree.load(path);

        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                  System.out.println("[Auto-Save] Saving facility data to disk...");
                mainTree.commit(); //
            }
        }));
    }

    public BPlusTree<String, Facility> getTree() {
        return mainTree;
    }

    public void createFacility(Facility newFacility) {
        mainTree.create(newFacility.getId(), newFacility);
    }

    public Facility readFacility(String id) {
        return mainTree.read(id);
    }

    public void updateBook(Facility UpdatedFacility) {
        mainTree.update(UpdatedFacility.getId(), UpdatedFacility);

    }

    public void deleteFacility(String id) {
        mainTree.delete(id);
    }

    public BPlusTree.SimpleList<Facility> getAllFacilities() {
        return mainTree.sort();
    }
}
