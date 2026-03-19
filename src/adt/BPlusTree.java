/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

import dao.FileDAO;
import java.io.Serializable;

/**
 *
 * @author asus-z
 * @param <K>
 * @param <V>
 */
public class BPlusTree<K extends Comparable<K> & Serializable, V extends Serializable> implements Serializable {

    private Node<K, V> root;
    private int M = 3;
    private transient FileDAO<K, V> dao;

    // --- Constructor ---
    public BPlusTree(int m, FileDAO<K, V> dao) {
        if (m < 3) {
            throw new IllegalArgumentException("M must be at least 3.");
        }
        this.M = m;
        this.root = new LeafNode<>(M);
        this.dao = dao;
    }

    public BPlusTree() {
        this.root = new LeafNode<>(M);
    }

    public void setDAO(FileDAO<K, V> dao) {
        this.dao = dao;
    }

    public void commit() {
        if (dao != null) {
            dao.save(this);
        } else {
            System.out.println("No DAO instance found, cannot save.");
        }
    }

    // --- (CRUD) ---
    public void create(K key, V value) {
        LeafNode<K, V> leaf = findLeaf(root, key);
        int idx = binarySearch(leaf.keys, leaf.currentKeyCount, key);

        if (idx >= 0) {
            leaf.values[idx] = value; // Update if exists
            return;
        }

        int pos = -(idx + 1);
        leaf.insertAt(pos, key, value);

        if (leaf.isOverflow()) {
            splitLeaf(leaf);
        }
    }
    // --- Internal Logic: Split ---

    private void splitLeaf(LeafNode<K, V> leaf) {
        int mid = leaf.currentKeyCount / 2;
        int numToMove = leaf.currentKeyCount - mid;
        LeafNode<K, V> sibling = new LeafNode<>(M);

        for (int i = 0; i < numToMove; i++) {
            sibling.keys[i] = leaf.keys[mid + i];
            sibling.values[i] = leaf.values[mid + i];
            leaf.keys[mid + i] = null;
            leaf.values[mid + i] = null;
        }
        sibling.currentKeyCount = numToMove;
        leaf.currentKeyCount = mid;

        // Doubly linked list connection logic
        sibling.next = leaf.next;
        if (leaf.next != null) {
            leaf.next.prev = sibling;
        }
        leaf.next = sibling;
        sibling.prev = leaf;

        pushUp(leaf, sibling, sibling.keys[0]);
    }

    private void splitInternal(InternalNode<K, V> node) {
        int mid = node.currentKeyCount / 2;
        K upKey = node.keys[mid];
        InternalNode<K, V> sibling = new InternalNode<>(M);

        int numToMove = node.currentKeyCount - mid - 1;
        for (int i = 0; i < numToMove; i++) {
            sibling.keys[i] = node.keys[mid + 1 + i];
            sibling.children[i] = node.children[mid + 1 + i];
            sibling.children[i].parent = sibling;
            node.keys[mid + 1 + i] = null;
            node.children[mid + 1 + i] = null;
        }
        sibling.children[numToMove] = node.children[node.currentKeyCount];
        sibling.children[numToMove].parent = sibling;
        node.children[node.currentKeyCount] = null;

        sibling.currentKeyCount = numToMove;
        node.currentKeyCount = mid;

        pushUp(node, sibling, upKey);
    }

    private void pushUp(Node<K, V> left, Node<K, V> right, K key) {
        if (left.parent == null) {
            InternalNode<K, V> newRoot = new InternalNode<>(M);
            newRoot.keys[0] = key;
            newRoot.children[0] = left;
            newRoot.children[1] = right;
            newRoot.currentKeyCount = 1;
            left.parent = newRoot;
            right.parent = newRoot;
            this.root = newRoot;
            return;
        }

        InternalNode<K, V> parent = (InternalNode<K, V>) left.parent;
        int idx = binarySearch(parent.keys, parent.currentKeyCount, key);
        int pos = -(idx + 1);

        parent.insertAt(pos, key, right);
        if (parent.isOverflow()) {
            splitInternal(parent);
        }
    }

    public V read(K key) {
        if (key == null) {
            return null;
        }
        LeafNode<K, V> leaf = findLeaf(root, key);
        int idx = binarySearch(leaf.keys, leaf.currentKeyCount, key);
        return (idx >= 0) ? leaf.values[idx] : null;
    }

    private int binarySearch(K[] arr, int count, K key) {
        int low = 0, high = count - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = arr[mid].compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -(low + 1);
    }

    private LeafNode<K, V> findLeaf(Node<K, V> node, K key) {
        if (node instanceof LeafNode) {
            return (LeafNode<K, V>) node;
        }

        InternalNode<K, V> internal = (InternalNode<K, V>) node;
        int idx = binarySearch(internal.keys, internal.currentKeyCount, key);
        int childIdx = (idx >= 0) ? idx + 1 : -(idx + 1);
        return findLeaf(internal.children[childIdx], key);
    }

    public void update(K key, V value) {
        create(key, value); // In B+ tree，operator Update is same with Create
    }

    public boolean delete(K key) {
        LeafNode<K, V> leaf = findLeaf(root, key);
        int idx = binarySearch(leaf.keys, leaf.currentKeyCount, key);

        if (idx >= 0) {
            leaf.removeAt(idx);
            // If it is the root node, it is valid even if it contains only one element, unless the root becomes empty.
            if (leaf == root) {
                if (leaf.currentKeyCount == 0) {
                    root = null;
                }
                return true;
            }

            // Check if an underflow has occurred (the number of keys is less than Math.ceil(M/2.0) - 1).
            if (leaf.isUnderflow()) {
                handleUnderflow(leaf);
            }
            return true;
        }
        return false;
    }

    private void handleUnderflow(Node<K, V> node) {
        InternalNode<K, V> parent = (InternalNode<K, V>) node.parent;
        int childIdx = parent.indexOfChild(node);
        System.out.println(childIdx);
        // 1. Borrow from Left Sibling
        if (childIdx > 0) {
            Node<K, V> leftSibling = parent.children[childIdx - 1];
            if (leftSibling.canLend()) {
                borrowFromLeft(node, leftSibling, parent, childIdx);
                return;
            }
        }

        // 2. Borrow from Right Sibling
        if (childIdx < parent.currentKeyCount - 1) {
            Node<K, V> rightSibling = parent.children[childIdx + 1];
            if (rightSibling.canLend()) {
                borrowFromRight(node, rightSibling, parent, childIdx);
                return;
            }
        }

        // 3. (Merge)
        if (childIdx > 0) {
            // Merging with the Left Sibling
            merge(parent.children[childIdx - 1], node, parent, childIdx);
        } else {
            // Merging with the Right Sibling
            merge(node, parent.children[childIdx + 1], parent, childIdx + 1);
        }
    }

    private void borrowFromLeft(Node<K, V> node, Node<K, V> leftSibling, InternalNode<K, V> parent, int childIdx) {
        if (node instanceof LeafNode) {
            LeafNode<K, V> leaf = (LeafNode<K, V>) node;
            LeafNode<K, V> left = (LeafNode<K, V>) leftSibling;

            // 1. Take the last key and value from the left sibling.
            K borrowedKey = left.keys[left.currentKeyCount - 1];
            V borrowedValue = left.values[left.currentKeyCount - 1];
            left.removeAt(left.currentKeyCount - 1);

            // 2.Insert at the beginning of the current leaf node
            leaf.insertAt(0, borrowedKey, borrowedValue);

            // 3. Update the separator in the parent node (Separator)
            parent.keys[childIdx - 1] = leaf.keys[0];

        } else {
            InternalNode<K, V> internal = (InternalNode<K, V>) node;
            InternalNode<K, V> left = (InternalNode<K, V>) leftSibling;

            // 1. Internal node rotation: The parent node's key is moved to the front of the current node.
            K parentKey = parent.keys[childIdx - 1];
            Node<K, V> borrowedChild = left.children[left.currentKeyCount]; // The last son of the left sibling on the left

            internal.insertKeyAndChildAtFirst(parentKey, borrowedChild);

            // 2. The last key of the left sibling is promoted to the parent node.
            parent.keys[childIdx - 1] = left.keys[left.currentKeyCount - 1];

            // 3. Remove the extra tail from the left sibling
            left.removeLast();
        }
    }

    private void borrowFromRight(Node<K, V> node, Node<K, V> rightSibling, InternalNode<K, V> parent, int childIdx) {
        if (node instanceof LeafNode) {
            LeafNode<K, V> leaf = (LeafNode<K, V>) node;
            LeafNode<K, V> right = (LeafNode<K, V>) rightSibling;

            // 1.Take the first key and value from the right sibling.
            K borrowedKey = right.keys[0];
            V borrowedValue = right.values[0];
            right.removeAt(0);

            // 2. Place at the end of the current node
            leaf.insertAt(leaf.currentKeyCount, borrowedKey, borrowedValue);

            // 3. Update the parent node's key to the new first value of its right sibling.
            parent.keys[childIdx] = right.keys[0];

        } else {
            InternalNode<K, V> internal = (InternalNode<K, V>) node;
            InternalNode<K, V> right = (InternalNode<K, V>) rightSibling;

            // 1. Internal node rotation: The parent node's key is moved to the end of the current node.
            K parentKey = parent.keys[childIdx];
            Node<K, V> borrowedChild = right.children[0];

            internal.insertAt(internal.currentKeyCount, parentKey, borrowedChild);

            // 2. The first key of the right sibling is promoted to the parent node.
            parent.keys[childIdx] = right.keys[0];

            // 3. Move the right sibling to the front (delete the first key and child).
            right.removeFirst();
        }
    }

    private void merge(Node<K, V> left, Node<K, V> right, InternalNode<K, V> parent, int rightIdx) {
        // 1. Get the key in the parent node that is sandwiched between the left and right keys.
        K parentKey = parent.keys[rightIdx - 1];

        // 2. Merge
        left.combineWith(right, parentKey);

        // 3. Remove the key from the parent node that has been moved down and lost its right child.
        parent.removeKeyAndChildAt(rightIdx - 1);

        // 4. Recursively process the parent node's Underflow
        if (parent != root && parent.isUnderflow()) {
            handleUnderflow(parent);
        } else if (parent == root && parent.currentKeyCount == 0) {
            // If the root node is drained, let left become the new root.
            root = left;
            root.parent = null;
        }
    }

    public static class SimpleList<T> {

        private Object[] elements = new Object[10];
        private int size = 0;

        public void add(T item) {
            if (size == elements.length) {
                Object[] newArr = new Object[elements.length * 2];
                System.arraycopy(elements, 0, newArr, 0, size);
                elements = newArr;
            }
            elements[size++] = item;
        }

        public int size() {
            return size;
        }

        @SuppressWarnings("unchecked")
        public T get(int i) {
            return (T) elements[i];
        }
    }

    public SimpleList<V> searchRange(K low, K high) {
        SimpleList<V> result = new SimpleList<>();
        LeafNode<K, V> curr = findLeaf(root, low);

        while (curr != null) {
            for (int i = 0; i < curr.currentKeyCount; i++) {
                K k = curr.keys[i];
                if (k.compareTo(high) > 0) {
                    return result;
                }
                if (k.compareTo(low) >= 0) {
                    result.add(curr.values[i]);
                }
            }
            curr = curr.next; // Go right along the linked list
        }
        return result;
    }

    public SimpleList<V> sort() {
        SimpleList<V> result = new SimpleList<>();
        // 找到最左边的叶子节点
        Node<K, V> curr = root;
        while (curr instanceof InternalNode) {
            curr = ((InternalNode<K, V>) curr).children[0];
        }
        // 遍历双向链表
        LeafNode<K, V> leaf = (LeafNode<K, V>) curr;
        while (leaf != null) {
            for (int i = 0; i < leaf.currentKeyCount; i++) {
                result.add(leaf.values[i]);
            }
            leaf = leaf.next;
        }
        return result;
    }

    // Queue，use in printTree
    private static class SimpleQueue<T> {

        private Object[] data = new Object[1000];
        private int head = 0, tail = 0, count = 0;

        public void add(T item) {
            data[tail] = item;
            tail = (tail + 1) % data.length;
            count++;
        }

        public T poll() {
            if (isEmpty()) {
                return null;
            }
            T item = (T) data[head];
            data[head] = null;
            head = (head + 1) % data.length;
            count--;
            return item;
        }

        public boolean isEmpty() {
            return count == 0;
        }

        public int size() {
            return count;
        }
    }

    public void printTree() {
        SimpleQueue<Node<K, V>> queue = new SimpleQueue<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Node<K, V> node = queue.poll();
                // Print all nodes of the current node Key
                System.out.print("[");
                for (int j = 0; j < node.currentKeyCount; j++) {
                    System.out.print(node.keys[j] + (j == node.currentKeyCount - 1 ? "" : ", "));
                }
                System.out.print("] ");

                if (node instanceof InternalNode) {
                    InternalNode<K, V> in = (InternalNode<K, V>) node;
                    for (int j = 0; j <= in.currentKeyCount; j++) {
                        if (in.children[j] != null) {
                            queue.add(in.children[j]);
                        }
                    }
                }
            }
            System.out.println();
        }
    }
}
