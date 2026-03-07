/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author asus-z
 */

abstract class Node<K extends Comparable<K>, V> {
    // use List use for insert and delete operation
    protected K[] keys;
    protected int currentKeyCount;
    protected Node<K, V> parent;

    public Node(int M) {
        this.keys = (K[]) new Comparable[M]; 
        this.currentKeyCount = 0;
        this.parent = null;
    }

    //get number of key for current node
    public int keyCount() {
        return currentKeyCount;
    }

    //Determine if overflow occurs(M is the order) 
    public boolean isOverflow(int M) {
        return currentKeyCount >= M;
    }

    //Determne if velue lower than lower limit (use for delete operator)
    public boolean isUnderflow(int M) {
       int minKeys = (M + 1) / 2 - 1; 
       return currentKeyCount < minKeys;
    }
    
    
    public boolean canLend(int M) {
        int minKeys = (int) Math.ceil(M / 2.0) - 1;
        return this.currentKeyCount > minKeys;
    }
    
    public abstract void combineWith(Node<K, V> rightSibling, K parentKey);
//    protected void insertAtIndex(int idx, K key) {
//        for (int i = currentKeyCount; i > idx; i--) {
//            keys[i] = keys[i - 1];
//        }
//        keys[idx] = key;
//        currentKeyCount++;
//    }
}