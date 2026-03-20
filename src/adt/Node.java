/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

import java.io.Serializable;

/**
 *
 * @author asus-z
 * @param <K>
 * @param <V>
 */
public abstract class Node<K extends Comparable<K> & Serializable, V extends Serializable> implements Serializable {

    // use List use for insert and delete operation
    protected K[] keys;
    protected int currentKeyCount;
    protected Node<K, V> parent;
    protected final int M;
    protected final int minKeys;
    private static final long serialVersionUID = 1L;

    public Node(int M) {
        this.keys = (K[]) new Comparable[M];
        this.currentKeyCount = 0;
        this.parent = null;
        this.M = M;
        this.minKeys = (M + 1) / 2 - 1;
    }

    //get number of key for current node
    public int keyCount() {
        return currentKeyCount;
    }

    //Determine if overflow occurs(M is the order) 
    public boolean isOverflow() {
        return currentKeyCount >= M;
    }

    //Determne if velue lower than lower limit (use for delete operator)
    public boolean isUnderflow() {
        return currentKeyCount < minKeys;
    }

    public boolean canLend() {
        return this.currentKeyCount > minKeys;
    }

    public abstract void combineWith(Node<K, V> rightSibling, K parentKey);

}
