/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author asus-z
 */


class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {
    protected V[] values;
    protected LeafNode<K, V> next; // Brother pointers used for range queries
    protected LeafNode<K, V> prev;
    
    public LeafNode(int M) {
        super(M);
        this.values = (V[]) new Object[M];
        this.next = null;
        this.prev = null;
    }
    
    
    
    protected void insertAt(int pos, K key, V value) {
        for (int i = currentKeyCount; i > pos; i--) {
            keys[i] = keys[i - 1];
            values[i] = values[i - 1];
        }
        keys[pos] = key;
        values[pos] = value;
        currentKeyCount++;
    }

    protected void removeAt(int idx) {
        for (int i = idx; i < currentKeyCount - 1; i++) {
            keys[i] = keys[i + 1];
            values[i] = values[i + 1];
        }
        keys[currentKeyCount - 1] = null;
        values[currentKeyCount - 1] = null;
        currentKeyCount--;
    }
    
    @Override
    public void combineWith(Node<K, V> right, K parentKey) {
        LeafNode<K, V> r = (LeafNode<K, V>) right;
        for (int i = 0; i < r.currentKeyCount; i++) {
            this.keys[this.currentKeyCount] = r.keys[i];
            this.values[this.currentKeyCount] = r.values[i];
            this.currentKeyCount++;
        }
        // 更新链表指针，跳过被合并掉的右兄弟
        this.next = r.next;
    }
}
