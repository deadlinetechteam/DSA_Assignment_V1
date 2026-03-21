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
                    if (oldIds.size() == 0) {
                        titleIndex.delete(oldBook.getTitle());
                    }
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

    public SimpleList<Book> searchByID(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }
        SimpleList<Book> results = mainTree.searchRange(keyword, keyword + "\uffff");
        return results;
    }

    public SimpleList<Book> searchByTitle(String keyword) {
        SimpleList<Book> results = new SimpleList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }

        // 1. Prefix matching using searchRange
        SimpleList<SimpleList<String>> idLists = titleIndex.searchRange(keyword, keyword + "\uffff");

        // 3. Iterate through the list of IDs corresponding to each title found in the search 
        for (int i = 0; i < idLists.size(); i++) {
            SimpleList<String> ids = idLists.get(i);

            // 4.Key Lookup operation
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

    public SimpleList<Book> searchByAvailability(String keyword) {
        SimpleList<Book> results = new SimpleList<>();

        SimpleList<Book> allBooks = mainTree.sort();

        for (int i = 0; i < allBooks.size(); i++) {
            Book b = allBooks.get(i);
            if (b.getAvailability() != null && b.getAvailability().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(b);
            }
        }
        return results;
    }
}
