/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.io.*;
import adt.*;

/**
 *
 * @author asus-z
 * @param <K>
 * @param <V>
 */

public class FileDAO<K extends Comparable<K> & Serializable, V extends Serializable> {
    private String filePath;

    public FileDAO(String filePath) {
        this.filePath = filePath;
    }

    
    public void save(BPlusTree<K, V> tree) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(tree);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
    @SuppressWarnings("unchecked")
    public BPlusTree<K, V> load() {
        File file = new File(filePath);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (BPlusTree<K, V>) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}