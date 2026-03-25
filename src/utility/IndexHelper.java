/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author asus-z
 */
import adt.BPlusTree;
import adt.BPlusTree.SimpleList;

public class IndexHelper {

    public static void addToIndex(BPlusTree<String, SimpleList<String>> index, String key, String id) {
        if (key == null || key.trim().isEmpty()) {
            key = "N/A";
        }
        SimpleList<String> ids = index.read(key);
        if (ids == null) {
            ids = new SimpleList<>();
            index.create(key, ids);
        }

        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    public static void removeFromIndex(BPlusTree<String, SimpleList<String>> index, String key, String id) {
        if (key == null || key.trim().isEmpty()) {
            key = "N/A";
        }
        SimpleList<String> ids = index.read(key);
        if (ids != null) {
            ids.remove(id);
            if (ids.size() == 0) {
                index.delete(key);
            }
        }
    }
}
