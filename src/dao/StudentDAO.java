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
import entitiy.Student;
import java.io.*;

public class StudentDAO {

    private String fileName = "students.dat";
    private static BPlusTree<String, Student> tree = new BPlusTree<>(4);

    public StudentDAO() {
        BPlusTree.SimpleList<Student> list = retrieveFromFile();
        for (int i = 0; i < list.size(); i++) {
            Student s = list.get(i);
            tree.create(s.getId(), s);
        }
    }

    public void saveToFile(BPlusTree.SimpleList<Student> list) {
        try (ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            ooStream.writeObject(list);
        } catch (IOException ex) {
            System.out.println("\nCannot save students");
        }
    }

    public BPlusTree.SimpleList<Student> retrieveFromFile() {
        File file = new File(fileName);
        if (!file.exists()) {
            return new BPlusTree.SimpleList<>();
        }
        try (ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file))) {
            return (BPlusTree.SimpleList<Student>) oiStream.readObject();
        } catch (Exception ex) {
            return new BPlusTree.SimpleList<>();
        }
    }
//
//    public void add(Student s) {
//        tree.create(s.getId(), s);
//        saveToFile(tree.sort());
//    }
//
//    public Student find(String id) {
//        return tree.search(id);
//    }
//
//    public void delete(Student s) {
//        tree.delete(s.getId());
//        saveToFile(tree.sort());
//    }
//
//    public BPlusTree.SimpleList<Student> getAll() {
//        return tree.sort();
//    }

}
