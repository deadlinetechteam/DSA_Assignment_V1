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

    public int indexOfChild(Node<K, V> node) {
        for (int i = 0; i < currentKeyCount; i++) {
            if (children[i] == node) {
                return i;
            }
        }
        return -1;
    }

    protected void insertKeyAndChildAtFirst(K key, Node<K, V> leftChild) {
        // 移动所有的 key 和 children 给第一个位置腾地
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
            // 1. 将所有 Key 向左移动一位
            for (int i = 0; i < currentKeyCount - 1; i++) {
                keys[i] = keys[i + 1];
            }
            keys[currentKeyCount - 1] = null; // 清除原本最后一位的残余

            // 2. 将所有 Children 指针向左移动一位
            // 注意：子节点数量是 currentKeyCount + 1，所以移动到 currentKeyCount
            for (int i = 0; i < currentKeyCount; i++) {
                children[i] = children[i + 1];
            }
            children[currentKeyCount] = null; // 清除原本最后一位的残余

            // 3. 计数减一
            currentKeyCount--;
        }
    }

    // 移除最后一个 Key 和对应的子节点（用于被借调时）
    protected void removeLast() {
        if (currentKeyCount > 0) {
            keys[currentKeyCount - 1] = null;
            children[currentKeyCount] = null;
            currentKeyCount--;
        }
    }

    @Override
    public void combineWith(Node<K, V> right, K parentKey) {
        InternalNode<K, V> r = (InternalNode<K, V>) right;
        // 1. 先把父节点下移的 key 放进来
        this.keys[this.currentKeyCount] = parentKey;
        this.currentKeyCount++;

        // 2. 把右兄弟的所有 keys 和 children 搬过来
        for (int i = 0; i < r.currentKeyCount; i++) {
            this.keys[this.currentKeyCount] = r.keys[i];
            this.children[this.currentKeyCount] = r.children[i];
            this.children[this.currentKeyCount].parent = this;
            this.currentKeyCount++;
        }
        // 搬最后一个 child
        this.children[this.currentKeyCount] = r.children[r.currentKeyCount];
        this.children[this.currentKeyCount].parent = this;
    }

    // 移除指定的 key 和它右侧的 child (用于父节点删除索引)
    public void removeKeyAndChildAt(int index) {
        // 移除 key
        for (int i = index; i < currentKeyCount - 1; i++) {
            keys[i] = keys[i + 1];
        }
        // 移除指向右兄弟的 child 指针
        for (int i = index + 1; i < currentKeyCount; i++) {
            children[i] = children[i + 1];
        }
        keys[currentKeyCount - 1] = null;
        children[currentKeyCount] = null;
        currentKeyCount--;
    }
}
