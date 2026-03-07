/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author asus-z
 */

import adt.BPlusTree;
import entitiy.Staff;
import java.io.*;

public class StaffDAO {
    private String fileName = "staffs.dat";
    private static BPlusTree<String, Staff> tree = new BPlusTree<>(4);

    public StaffDAO() {
        BPlusTree.SimpleList<Staff> list = retrieveFromFile();
        for (int i = 0; i < list.size(); i++) {
            Staff s = list.get(i);
            tree.create(s.getId(), s);
        }
    }

    public void saveToFile(BPlusTree.SimpleList<Staff> list) {
        try (ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ooStream.writeObject(list);
        } catch (IOException ex) {
            System.out.println("\nCannot save staffs");
        }
    }

    public BPlusTree.SimpleList<Staff> retrieveFromFile() {
        File file = new File(fileName);
        if (!file.exists()) return new BPlusTree.SimpleList<>();
        try (ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file))) {
            return (BPlusTree.SimpleList<Staff>) oiStream.readObject();
        } catch (Exception ex) {
            return new BPlusTree.SimpleList<>();
        }
    }

    public void add(Staff s) {
        tree.create(s.getId(), s);
        saveToFile(tree.sort());
    }

    public Staff find(String id) {
        return tree.search(id);
    }
    
      public BPlusTree.SimpleList<Staff> getAll() {
        return tree.sort();
    }

}
