/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */


import entitiy.Book;
import adt.BPlusTree;

public class BookManager {
    private final BPlusTree<String,Book> mainTree=new BPlusTree<>(4);
    // 入库新书
    
    public void createBook(Book newBook) {
        mainTree.create(newBook.getId(),newBook); 
    }

    // 根据ID查询
    public Book readBook(String id) {
        return mainTree.read(id);
    }
    
    // 更新书籍信息
    public void updateBook(Book UpdatedBook) {
        mainTree.update(UpdatedBook.getId(),UpdatedBook);
        
    }

    // 报废书籍 (触发 B+ 树删除逻辑)
    public void deleteBook(String id) {
        mainTree.delete(id);
    }

    // 获取所有书籍 (用于 GUI 表格刷新)
    public BPlusTree.SimpleList<Book> getAllBooks() {
        return mainTree.sort();
    }
}