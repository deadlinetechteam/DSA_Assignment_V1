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
import adt.BPlusTree.SimpleList;

public class StudentManager {

    private final BPlusTree<String, Student> mainTree;
    private BPlusTree<String, SimpleList<String>> nameIndex;
    private BPlusTree<String, SimpleList<String>> programmeIndex;
    private int nextIdNum;

    public StudentManager() {
        String path = "students.bin";
        BPlusTree<String, Student> loadedTree = BPlusTree.load(path);

        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        this.nextIdNum = calculateInitialCounter();
        rebuildIndices();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                System.out.println("[Auto-Save] Saving student data to disk...");
                mainTree.commit();
            }
        }));
    }

    public BPlusTree<String, Student> getTree() {
        return mainTree;
    }

    private int calculateInitialCounter() {
        SimpleList<Student> all = mainTree.sort();
        int max = 0;
        for (int i = 0; i < all.size(); i++) {
            try {

                int currentNum = Integer.parseInt(all.get(i).getId().substring(2));
                if (currentNum > max) {
                    max = currentNum;
                }
            } catch (NumberFormatException e) {
            }
        }
        return max;
    }

    public String generateNextId() {
        nextIdNum++;
        return String.format("ST%03d", nextIdNum);
    }

    public void createStudent(Student s) {
        mainTree.create(s.getId(), s);
        addNameToIndex(s.getName(), s.getId());
        addProgrammeToIndex(s.getProgramme(), s.getId());
    }

    public Student readStudent(String id) {
        return mainTree.read(id);
    }

    public void updateStudent(Student updatedS) {
        Student oldS = mainTree.read(updatedS.getId());
        if (oldS != null) {
            // If the name changes, update the name index.
            if (!oldS.getName().equals(updatedS.getName())) {
                removeFromIndex(nameIndex, oldS.getName(), oldS.getId());
                addNameToIndex(updatedS.getName(), updatedS.getId());
            }
            // If your major changes, update the major index.
            if (!oldS.getProgramme().equals(updatedS.getProgramme())) {
                removeFromIndex(programmeIndex, oldS.getProgramme(), oldS.getId());
                addProgrammeToIndex(updatedS.getProgramme(), updatedS.getId());
            }
        }
        mainTree.update(updatedS.getId(), updatedS);
    }

    public void deleteStudent(String id) {
        Student s = mainTree.read(id);
        if (s != null) {
            removeFromIndex(nameIndex, s.getName(), id);
            removeFromIndex(programmeIndex, s.getProgramme(), id);
            mainTree.delete(id);
        }
    }

    public boolean authenticate(String id, String password) {
        Student s = mainTree.read(id);
        return s != null && s.getPassword().equals(password);
    }

    public SimpleList<Student> getAllStudents() {
        return mainTree.sort();
    }

    // --- Search logic ---
    public SimpleList<Student> searchByID(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllStudents();
        }
        return mainTree.searchRange(keyword, keyword + "\uffff");
    }

    public SimpleList<Student> searchByName(String keyword) {
        return searchFromSecondaryIndex(nameIndex, keyword);
    }

    public SimpleList<Student> searchByProgramme(String keyword) {
        return searchFromSecondaryIndex(programmeIndex, keyword);
    }

    // --- Internal index maintenance tools ---
    private void rebuildIndices() {
        this.nameIndex = new BPlusTree<>(10);
        this.programmeIndex = new BPlusTree<>(10);
        SimpleList<Student> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            Student s = all.get(i);
            addNameToIndex(s.getName(), s.getId());
            addProgrammeToIndex(s.getProgramme(), s.getId());
        }
    }

    private void addNameToIndex(String name, String id) {
        SimpleList<String> ids = nameIndex.read(name);
        if (ids == null) {
            ids = new SimpleList<>();
            nameIndex.create(name, ids);
        }
        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    private void addProgrammeToIndex(String prog, String id) {
        SimpleList<String> ids = programmeIndex.read(prog);
        if (ids == null) {
            ids = new SimpleList<>();
            programmeIndex.create(prog, ids);
        }
        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    private void removeFromIndex(BPlusTree<String, SimpleList<String>> index, String key, String id) {
        SimpleList<String> ids = index.read(key);
        if (ids != null) {
            ids.remove(id);
            if (ids.size() == 0) {
                index.delete(key);
            }
        }
    }

    private SimpleList<Student> searchFromSecondaryIndex(BPlusTree<String, SimpleList<String>> index, String keyword) {
        SimpleList<Student> results = new SimpleList<>();
        SimpleList<SimpleList<String>> idLists = index.searchRange(keyword, keyword + "\uffff");
        for (int i = 0; i < idLists.size(); i++) {
            SimpleList<String> ids = idLists.get(i);
            for (int j = 0; j < ids.size(); j++) {
                Student s = mainTree.read(ids.get(j));
                if (s != null) {
                    results.add(s);
                }
            }
        }
        return results;
    }

}
