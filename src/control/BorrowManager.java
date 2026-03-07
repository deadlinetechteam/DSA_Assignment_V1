/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */

import dao.BookDAO;
import dao.BorrowDAO;
import entitiy.Book;
import entitiy.BorrowRecord;
import adt.BPlusTree;
import java.time.LocalDate;

public class BorrowManager {
    private BookDAO bookDAO = new BookDAO();
    private BorrowDAO borrowDAO = new BorrowDAO();

    // 借书逻辑
    public boolean borrowBook(String studentId, String studentName, String bookId) {
        Book book = bookDAO.find(bookId);
        
        // 1. 检查书籍是否存在且可用
        if (book == null || !book.getAvailability().equalsIgnoreCase("Available")) {
            return false;
        }

        // 2. 更新书籍状态
        book.setAvailability("Borrowed by " + studentId);
        bookDAO.addOrUpdate(book);

        // 3. 创建并储存借阅记录
        String txId = "TX" + System.currentTimeMillis(); // 简单生成唯一ID
        String today = LocalDate.now().toString();
        String dueDate = LocalDate.now().plusDays(14).toString(); // 默认借期14天
        
        BorrowRecord record = new BorrowRecord(txId, bookId, book.getTitle(), 
                                              studentId, studentName, today, dueDate, "On Loan");
        borrowDAO.add(record);
        return true;
    }

    // 还书逻辑
    public boolean returnBook(String bookId) {
        Book book = bookDAO.find(bookId);
        if (book == null) return false;

        // 1. 将书籍设为可用
        book.setAvailability("Available");
        bookDAO.addOrUpdate(book);

        // 2. 更新对应的借阅记录状态（实际项目中可根据 bookId 查找未完成的记录）
        // 这里简化处理：书籍恢复可用即代表归还
        return true;
    }

    public BPlusTree.SimpleList<BorrowRecord> getAllRecords() {
        return borrowDAO.getAll();
    }
}
