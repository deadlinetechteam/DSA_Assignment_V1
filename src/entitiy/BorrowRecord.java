/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entitiy;

import java.io.Serializable;

/**
 *
 * @author asus-z
 */
public class BorrowRecord implements Serializable {

    private String transactionId;
    private String bookId;
    private String bookTitle;
    private String studentId;
    private String studentName;
    private String borrowDate;
    private String dueDate;
    private String status;
    private static final long serialVersionUID = 1L;

    public BorrowRecord(String transactionId, String bookId, String bookTitle, String studentId, String studentName, String borrowDate, String dueDate, String status) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
