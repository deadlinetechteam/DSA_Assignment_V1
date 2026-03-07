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
import entitiy.BookingRecord;
import java.io.*;

public class BookingDAO {

    private String fileName = "bookings.dat";
    private static BPlusTree<String, BookingRecord> tree = new BPlusTree<>(4);

    public BookingDAO() {
        BPlusTree.SimpleList<BookingRecord> list = retrieveFromFile();
        for (int i = 0; i < list.size(); i++) {
            BookingRecord r = list.get(i);
            tree.create(r.getId(), r);
        }
    }

    public void saveToFile(BPlusTree.SimpleList<BookingRecord> list) {
        try (ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ooStream.writeObject(list);
        } catch (IOException ex) {
            System.out.println("\nCannot save bookings");
        }
    }

    public BPlusTree.SimpleList<BookingRecord> retrieveFromFile() {
        File file = new File(fileName);
        if (!file.exists()) {
            return new BPlusTree.SimpleList<>();
        }
        try (ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file))) {
            return (BPlusTree.SimpleList<BookingRecord>) oiStream.readObject();
        } catch (Exception ex) {
            return new BPlusTree.SimpleList<>();
        }
    }

    public void add(BookingRecord r) {
        tree.create(r.getId(), r);
        saveToFile(tree.sort());
    }

    public void delete(String id) {
        tree.delete(id);
        saveToFile(tree.sort());
    }

    public BookingRecord findById(String id) {
        return tree.search(id);
    }

    public BPlusTree.SimpleList<BookingRecord> getAll() {
        return tree.sort();
    }
}
