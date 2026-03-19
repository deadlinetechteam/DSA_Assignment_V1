/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package utility;

/**
 *
 * @author asus-z
 */

import adt.BPlusTree;


public class DSA_Assignment_V1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        BPlusTree<Integer, String> tree = new BPlusTree<>(3);

        // --- CREATE (Insert) ---
        System.out.println("Inserting data...");
        for (int i=1;i<100 ;i+=1){
        tree.create(i, "User_"+i);
        
        }
        tree.create(2,"test");
        // --- READ (Search) ---
        String result = tree.read(99);
        System.out.println("Search for key 15: " + result); // Output: User_15

        // --- UPDATE ---
        // Inserting an existing key replaces the value
        tree.create(15, "Updated_User_15");
        System.out.println("Updated key 15: " + tree.read(15));

        // --- DELETE ---
         for (int i=1;i<9 ;i+=1){
        tree.delete(i);
        
        }
        tree.delete(36);
        System.out.println(tree.read(37));
        System.out.println("Key 10 deleted. Search result: " + tree.read(10));

        // --- RANGE QUERY (Order) ---
        System.out.println("Range Search (Keys 5 to 25):");
        BPlusTree.SimpleList<String> rangeResults = tree.searchRange(5, 25);
        System.out.print("["); 
        for (int i = 0; i < rangeResults.size(); i++) {
            System.out.print(rangeResults.get(i));
            
            if (i < rangeResults.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");

        // --- VISUALIZE ---
        System.out.println("Tree Structure:");
        tree.printTree(); 
    }
    
}
