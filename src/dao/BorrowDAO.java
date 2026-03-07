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
import entitiy.BorrowRecord;
import java.io.*;

public class BorrowDAO {
    private String fileName = "borrows.dat";
    private static BPlusTree<String, BorrowRecord> tree = new BPlusTree<>(4);

    public BorrowDAO() {
        BPlusTree.SimpleList<BorrowRecord> list = retrieveFromFile();
        for (int i = 0; i < list.size(); i++) {
            BorrowRecord r = list.get(i);
            tree.create(r.getTransactionId(), r);
        }
    }

    public void saveToFile(BPlusTree.SimpleList<BorrowRecord> list) {
        try (ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ooStream.writeObject(list);
        } catch (IOException ex) {
            System.out.println("\n无法保存借阅记录");
        }
    }

    public BPlusTree.SimpleList<BorrowRecord> retrieveFromFile() {
        File file = new File(fileName);
        if (!file.exists()) return new BPlusTree.SimpleList<>();
        try (ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file))) {
            return (BPlusTree.SimpleList<BorrowRecord>) oiStream.readObject();
        } catch (Exception ex) {
            return new BPlusTree.SimpleList<>();
        }
    }

    public void add(BorrowRecord r) {
        tree.create(r.getTransactionId(), r);
        saveToFile(tree.sort());
    }

    public void delete(String transactionId) {
        tree.delete(transactionId);
        saveToFile(tree.sort());
    }

    public BPlusTree.SimpleList<BorrowRecord> getAll() {
        return tree.sort();
    }
}
