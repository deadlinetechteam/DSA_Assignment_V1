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
import entitiy.Facility;
import java.io.*;

public class FacilityDAO {
    private String fileName = "facilities.dat";
    private static BPlusTree<String, Facility> tree = new BPlusTree<>(4);

    public FacilityDAO() {
        // 初始化时从文件恢复数据并重建 B+ 树
        BPlusTree.SimpleList<Facility> list = retrieveFromFile();
        for (int i = 0; i < list.size(); i++) {
            Facility f = list.get(i);
            tree.create(f.getId(), f);
        }
    }

    public void saveToFile(BPlusTree.SimpleList<Facility> list) {
        File file = new File(fileName);
        try (ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(file))) {
            ooStream.writeObject(list);
        } catch (IOException ex) {
            System.out.println("\nCannot save facilities to file");
        }
    }

    public BPlusTree.SimpleList<Facility> retrieveFromFile() {
        File file = new File(fileName);
        BPlusTree.SimpleList<Facility> list = new BPlusTree.SimpleList<>();
        if (!file.exists()) return list;
        try (ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file))) {
            list = (BPlusTree.SimpleList<Facility>) oiStream.readObject();
        } catch (Exception ex) {
            System.out.println("\nCannot read facilities from file");
        }
        return list;
    }

//    // 提供给 Control 层的操作接口
//    public void add(Facility f) {
//        tree.create(f.getId(), f);
//        saveToFile(tree.sort());
//    }
//
//    public void delete(String id) {
//        tree.delete(id);
//        saveToFile(tree.sort());
//    }
//
//    public Facility findById(String id) {
//        return tree.search(id);
//    }
//
//    public BPlusTree.SimpleList<Facility> getAll() {
//        return tree.sort();
//    }
}