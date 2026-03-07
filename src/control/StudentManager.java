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
    private StudentDAO studentDAO = new StudentDAO();

    public boolean authenticate(String id, String password) {
        Student s = studentDAO.find(id);
        return s != null && s.getPassword().equals(password);
    }

    public void saveStudent(Student s) {
        studentDAO.add(s); // DAO 内部会执行 tree.create 并 saveToFile
    }
    
    public void deleteStudent(Student s) {
        studentDAO.delete(s); // DAO 内部执行 tree.delete (触发 B+ 树平衡)
    }
    
    public void updateStudent(Student s) {
        studentDAO.add(s);
    }
    
    public BPlusTree.SimpleList<Student> getAllStudents() {
        return studentDAO.getAll(); // DAO 内部执行 tree.sort()
    }
}
