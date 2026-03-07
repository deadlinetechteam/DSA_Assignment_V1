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
import entitiy.Book;
import java.io.*;

public class BookDAO {
    private String fileName = "books.dat";
    private static BPlusTree<String, Book> tree = new BPlusTree<>(4);

    public BookDAO() {
        // 加载现有书籍数据到 B+ 树
        BPlusTree.SimpleList<Book> list = retrieveFromFile();
        for (int i = 0; i < list.size(); i++) {
            Book b = list.get(i);
            tree.create(b.getId(), b);
        }
    }

    public void saveToFile(BPlusTree.SimpleList<Book> list) {
        try (ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ooStream.writeObject(list);
        } catch (IOException ex) {
            System.out.println("\n无法保存书籍数据");
        }
    }

    public BPlusTree.SimpleList<Book> retrieveFromFile() {
        File file = new File(fileName);
        if (!file.exists()) return new BPlusTree.SimpleList<>();
        try (ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file))) {
            return (BPlusTree.SimpleList<Book>) oiStream.readObject();
        } catch (Exception ex) {
            return new BPlusTree.SimpleList<>();
        }
    }

    public void addOrUpdate(Book b) {
        tree.create(b.getId(), b);
        saveToFile(tree.sort());
    }

    public void delete(String id) {
        tree.delete(id);
        saveToFile(tree.sort());
    }

    public Book find(String id) {
        return tree.search(id);
    }

    public BPlusTree.SimpleList<Book> getAll() {
        return tree.sort();
    }
}