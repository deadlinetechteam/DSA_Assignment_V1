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
import utility.IndexHelper;

public class BookManager {

    private final BPlusTree<String, Book> mainTree;
    private BPlusTree<String, SimpleList<String>> titleIndex;
    private BPlusTree<String, SimpleList<String>> categoryIndex;

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
        IndexHelper.addToIndex(titleIndex,newBook.getTitle(), newBook.getId());
        IndexHelper.addToIndex(categoryIndex,newBook.getCategory(), newBook.getId());
    }

    public Book readBook(String id) {
        return mainTree.read(id);
    }

    public void updateBook(Book updatedBook) {
        Book oldBook = mainTree.read(updatedBook.getId());
        if (oldBook != null) {
            if (!oldBook.getTitle().equals(updatedBook.getTitle())) {
                IndexHelper.removeFromIndex(titleIndex, oldBook.getTitle(), oldBook.getId());
                IndexHelper.addToIndex(titleIndex,updatedBook.getTitle(), updatedBook.getId());
            }
            if (!oldBook.getCategory().equals(updatedBook.getCategory())) {
                IndexHelper.removeFromIndex(categoryIndex, oldBook.getCategory(), oldBook.getId());
                IndexHelper.addToIndex(categoryIndex,updatedBook.getCategory(), updatedBook.getId());
            }
        }
        mainTree.update(updatedBook.getId(), updatedBook);

    }

    public void deleteBook(String id) {
        Book b = mainTree.read(id);
        if (b != null) {
            IndexHelper.removeFromIndex(titleIndex, b.getTitle(), id);
            IndexHelper.removeFromIndex(categoryIndex, b.getCategory(), id);
            mainTree.delete(id);
        }

    }

    public BPlusTree.SimpleList<Book> getAllBooks() {
        return mainTree.sort();
    }

    private void rebuildIndex() {
        this.titleIndex = new BPlusTree<>(10);
        this.categoryIndex = new BPlusTree<>(10);
        SimpleList<Book> allBooks = mainTree.sort();

        for (int i = 0; i < allBooks.size(); i++) {
            Book b = allBooks.get(i);
            IndexHelper.addToIndex(titleIndex,b.getTitle(), b.getId());
            IndexHelper.addToIndex(categoryIndex,b.getCategory(), b.getId());
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

    public SimpleList<Book> searchByCategory(String keyword) {
        SimpleList<Book> results = new SimpleList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }

        // 1. Prefix matching using searchRange
        SimpleList<SimpleList<String>> idLists = categoryIndex.searchRange(keyword, keyword + "\uffff");

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

    public SimpleList<Object[]> getCategoryReport() {

        SimpleList<String> allCategories = categoryIndex.sortKeys();
        SimpleList<SimpleList<String>> allValueLists = categoryIndex.sort();

        int totalBooks = 0;
        for (int i = 0; i < allValueLists.size(); i++) {
            totalBooks += allValueLists.get(i).size();
        }

        SimpleList<Object[]> reportRows = new SimpleList<>();
        for (int i = 0; i < allCategories.size(); i++) {
            String categoryName = allCategories.get(i);
            int count = allValueLists.get(i).size();
            double percent = (totalBooks == 0) ? 0 : (count * 100.0 / totalBooks);

            reportRows.add(new Object[]{
                categoryName,
                count,
                String.format("%.2f%%", percent)
            });
        }
        return reportRows;
    }
}
