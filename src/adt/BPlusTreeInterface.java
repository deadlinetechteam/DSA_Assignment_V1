/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author asus-z
 * @param <K>
 * @param <V>
 */
public interface BPlusTreeInterface<K, V>{
    public void create(K key, V value);
    public V read(K key);
    public void update(K key, V value);
      public boolean delete(K key);
      public BPlusTree.SimpleList<V> searchRange(K low, K high);
      public BPlusTree.SimpleList<V> sort();
}
