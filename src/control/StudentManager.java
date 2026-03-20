/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */
import entitiy.Student;
import adt.BPlusTree;

public class StudentManager {

    private final BPlusTree<String, Student> mainTree;

    public StudentManager() {
        String path = "students.bin";
        BPlusTree<String, Student> loadedTree = BPlusTree.load(path);

        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
               System.out.println("[Auto-Save] Saving student data to disk...");
            mainTree.commit();
        }));
    }

    public void saveStudent(Student s) {
        mainTree.create(s.getId(), s);
    }

    public Student readStudent(String id) {
        return mainTree.read(id);
    }

    public void deleteStudent(Student s) {
        mainTree.delete(s.getId());
    }

    public void updateStudent(Student s) {
        mainTree.create(s.getId(), s);
    }

    public BPlusTree<String, Student> getTree() {
        return mainTree;
    }

    public boolean authenticate(String id, String password) {
        Student s = mainTree.read(id);
        return s != null && s.getPassword().equals(password);
    }

    public BPlusTree.SimpleList<Student> getAllStudents() {
        return mainTree.sort();
    }
}
