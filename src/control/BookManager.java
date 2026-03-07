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
import entitiy.Book;
import adt.BPlusTree;

public class BookManager {
    private BookDAO bookDAO = new BookDAO();

    // 入库新书
    public void addBook(Book book) {
        bookDAO.addOrUpdate(book);
    }

    // 更新书籍信息
    public void updateBook(Book book) {
        bookDAO.addOrUpdate(book);
    }

    // 报废书籍 (触发 B+ 树删除逻辑)
    public void removeBook(String id) {
        bookDAO.delete(id);
    }

    // 根据ID查询
    public Book findBook(String id) {
        return bookDAO.find(id);
    }

    // 获取所有书籍 (用于 GUI 表格刷新)
    public BPlusTree.SimpleList<Book> getAllBooks() {
        return bookDAO.getAll();
    }
}