package jq.m04.treesbst;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A binary search tree of bike numbers, such as 1042 for bike B-1042.
 * Height counts nodes on the longest path from the root down:
 * an empty tree has height 0 and a single bike has height 1.
 * Build it by hand from Node objects. Do not use TreeMap, TreeSet or any sort.
 */
public class BikeRegistry {

    private static class Node {
        int bike;
        Node left;
        Node right;

        Node(int bike) {
            this.bike = bike;
        }
    }

    private Node root;
    private int size;

    public void insert(int bike) {
        // TODO 1: walk down from the root (smaller goes left, larger goes right).
        // Hang a new Node where you find null, add 1 to size, and ignore a duplicate.
    }

    public boolean contains(int bike) {
        // TODO 2: search recursively, going left or right, and return false at null.
        return false;
    }

    public List<Integer> inOrder() {
        // TODO 3: in-order traversal: left subtree, then the node, then the right subtree.
        return new ArrayList<>();
    }

    public int height() {
        // TODO 4: an empty tree is 0; otherwise 1 + the taller of the two subtrees.
        return 0;
    }

    public int min() {
        // TODO 5: throw NoSuchElementException when empty; otherwise keep going left.
        return 0;
    }

    public int max() {
        // TODO 6: throw NoSuchElementException when empty; otherwise keep going right.
        return 0;
    }

    public int size() {
        return size;
    }

    public static void main(String[] args) {
        BikeRegistry registry = new BikeRegistry();
        int[] arrivals = {1042, 1017, 1088, 1003, 1025, 1061, 1099};
        for (int bike : arrivals) {
            registry.insert(bike);
        }
        System.out.println("In id order: " + registry.inOrder());
        System.out.println("Has B-1025? " + registry.contains(1025));
        System.out.println("Height: " + registry.height());
    }
}
