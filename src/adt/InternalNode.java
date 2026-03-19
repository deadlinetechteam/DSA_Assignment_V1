/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author asus-z
 */
class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {

    protected Node<K, V>[] children;

    public InternalNode(int M) {
        super(M);
        this.children = (Node<K, V>[]) new Node[M + 1];
    }

    protected void insertAt(int pos, K key, Node<K, V> rightChild) {
        for (int i = currentKeyCount; i > pos; i--) {
            keys[i] = keys[i - 1];
            children[i + 1] = children[i];
        }
        keys[pos] = key;
        children[pos + 1] = rightChild;
        rightChild.parent = this;
        currentKeyCount++;
    }

    protected void insertKeyAndChildAtFirst(K key, Node<K, V> leftChild) {
        // Move all the keys and children to make room for the first position.
        for (int i = currentKeyCount; i > 0; i--) {
            keys[i] = keys[i - 1];
        }
        for (int i = currentKeyCount + 1; i > 0; i--) {
            children[i] = children[i - 1];
        }
        keys[0] = key;
        children[0] = leftChild;
        leftChild.parent = this;
        currentKeyCount++;
    }

    protected void removeFirst() {
        if (currentKeyCount > 0) {
            for (int i = 0; i < currentKeyCount - 1; i++) {
                keys[i] = keys[i + 1];
            }
            keys[currentKeyCount - 1] = null; 
            for (int i = 0; i < currentKeyCount; i++) {
                children[i] = children[i + 1];
            }
            children[currentKeyCount] = null;
            currentKeyCount--;
        }
    }

    // Remove the last key and its corresponding child nodes (for use when the key is seconded).
    protected void removeLast() {
        if (currentKeyCount > 0) {
            keys[currentKeyCount - 1] = null;
            children[currentKeyCount] = null;
            currentKeyCount--;
        }
    }

    // Remove the specified key and its child nodes to the right (used for deleting indexes on parent nodes).
    public void removeKeyAndChildAt(int index) {
        // remove key
        for (int i = index; i < currentKeyCount - 1; i++) {
            keys[i] = keys[i + 1];
        }
        // Remove the child pointer that points to the right sibling.
        for (int i = index + 1; i < currentKeyCount; i++) {
            children[i] = children[i + 1];
        }
        keys[currentKeyCount - 1] = null;
        children[currentKeyCount] = null;
        currentKeyCount--;
    }

    @Override
    public void combineWith(Node<K, V> right, K parentKey) {
        InternalNode<K, V> r = (InternalNode<K, V>) right;
        // 1. First, put in the key that was moved down from the parent node.
        this.keys[this.currentKeyCount] = parentKey;
        this.currentKeyCount++;

        // 2. Move all the keys and children of the right brother over.
        for (int i = 0; i < r.currentKeyCount; i++) {
            this.keys[this.currentKeyCount] = r.keys[i];
            this.children[this.currentKeyCount] = r.children[i];
            this.children[this.currentKeyCount].parent = this;
            this.currentKeyCount++;
        }
        // Move the last child
        this.children[this.currentKeyCount] = r.children[r.currentKeyCount];
        this.children[this.currentKeyCount].parent = this;
    }

    public int indexOfChild(Node<K, V> node) {
        for (int i = 0; i <= currentKeyCount; i++) {
            if (children[i] == node) {
                return i;
            }
        }
        return -1;
    }
}
