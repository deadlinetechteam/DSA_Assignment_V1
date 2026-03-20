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
import adt.BPlusTree.SimpleList;

public class BookManager {

    private final BPlusTree<String, Book> mainTree;
    private BPlusTree<String, SimpleList<String>> titleIndex;

    public BookManager() {
        String path = "Books.bin";
        BPlusTree<String, Book> loadedTree = BPlusTree.load(path);
        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        rebuildIndex();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                System.out.println("[Auto-Save] Saving book data to disk...");
                mainTree.commit();
            }
        }));
    }

    public BPlusTree<String, Book> getTree() {
        return mainTree;
    }

    public void createBook(Book newBook) {
        mainTree.create(newBook.getId(), newBook);
        addTitleToIndex(newBook.getTitle(), newBook.getId());
    }

    public Book readBook(String id) {
        return mainTree.read(id);
    }

    public void updateBook(Book updatedBook) {
        Book oldBook = mainTree.read(updatedBook.getId());
        if (oldBook != null) {
          
            if (!oldBook.getTitle().equals(updatedBook.getTitle())) {             
                SimpleList<String> oldIds = titleIndex.read(oldBook.getTitle());
                if (oldIds != null) {
                    oldIds.remove(oldBook.getId());
                }
                addTitleToIndex(updatedBook.getTitle(), updatedBook.getId());
            }
        }
        mainTree.update(updatedBook.getId(), updatedBook);

    }

    public void deleteBook(String id) {
        Book b = mainTree.read(id);
        if (b != null) {
            
            SimpleList<String> ids = titleIndex.read(b.getTitle());
            if (ids != null) {
                ids.remove(id); 
                if (ids.size() == 0) {
                    titleIndex.delete(b.getTitle());
                }
            }
           
            mainTree.delete(id);
        }
    }

    public BPlusTree.SimpleList<Book> getAllBooks() {
        return mainTree.sort();
    }

    private void rebuildIndex() {
        this.titleIndex = new BPlusTree<>(10);
        SimpleList<Book> allBooks = mainTree.sort();

        for (int i = 0; i < allBooks.size(); i++) {
            Book b = allBooks.get(i);
            addTitleToIndex(b.getTitle(), b.getId());
        }
    }

    private void addTitleToIndex(String title, String id) {
        SimpleList<String> ids = titleIndex.read(title);
        if (ids == null) {
            ids = new SimpleList<>();
            titleIndex.create(title, ids);
        }
        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    public SimpleList<Book> searchByTitle(String keyword) {
        SimpleList<Book> results = new SimpleList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }

        // 2. 利用 searchRange 进行前缀匹配 (例如输入 "Java" 匹配 "Java 8", "Java Web")
        // 范围是 [keyword, keyword + 最大字符]
        SimpleList<SimpleList<String>> idLists = titleIndex.searchRange(keyword, keyword + "\uffff");

        // 3. 遍历搜索到的每一个标题对应的 ID 列表 (处理重名)
        for (int i = 0; i < idLists.size(); i++) {
            SimpleList<String> ids = idLists.get(i);

            // 4. 遍历列表里的每一个 ID，执行“回表”操作
            for (int j = 0; j < ids.size(); j++) {
                String bookId = ids.get(j);
                Book b = mainTree.read(bookId);
                if (b != null) {
                    results.add(b);
                }
            }
        }
        return results;
    }
}
