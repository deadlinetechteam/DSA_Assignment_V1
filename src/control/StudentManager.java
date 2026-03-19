/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */

import dao.StudentDAO;
import entitiy.Student;
import adt.BPlusTree;

public class StudentManager {
     private final BPlusTree<String,Student> mainTree=new BPlusTree<>(4);
    private StudentDAO studentDAO = new StudentDAO();

    public boolean authenticate(String id, String password) {
        Student s = mainTree.read(id);
        return s != null && s.getPassword().equals(password);
    }

    public void saveStudent(Student s) {
        mainTree.create(s.getId(),s); // DAO 内部会执行 tree.create 并 saveToFile
    }
    
    public void deleteStudent(Student s) {
        mainTree.delete(s.getId()); // DAO 内部执行 tree.delete (触发 B+ 树平衡)
    }
    
    public void updateStudent(Student s) {
        mainTree.create(s.getId(),s);
    }
    
    public BPlusTree.SimpleList<Student> getAllStudents() {
        return mainTree.sort(); // DAO 内部执行 tree.sort()
    }
}
