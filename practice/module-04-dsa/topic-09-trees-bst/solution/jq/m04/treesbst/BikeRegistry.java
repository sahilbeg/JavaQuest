package jq.m04.treesbst;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A binary search tree of bike numbers, such as 1042 for bike B-1042.
 * Height counts nodes on the longest path from the root down:
 * an empty tree has height 0 and a single bike has height 1.
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
        root = insert(root, bike);
    }

    private Node insert(Node node, int bike) {
        if (node == null) {
            size++;
            return new Node(bike);
        }
        if (bike < node.bike) {
            node.left = insert(node.left, bike);
        } else if (bike > node.bike) {
            node.right = insert(node.right, bike);
        }
        return node;
    }

    public boolean contains(int bike) {
        return contains(root, bike);
    }

    private boolean contains(Node node, int bike) {
        if (node == null) {
            return false;
        }
        if (bike == node.bike) {
            return true;
        }
        if (bike < node.bike) {
            return contains(node.left, bike);
        }
        return contains(node.right, bike);
    }

    public List<Integer> inOrder() {
        List<Integer> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }

    private void inOrder(Node node, List<Integer> result) {
        if (node == null) {
            return;
        }
        inOrder(node.left, result);
        result.add(node.bike);
        inOrder(node.right, result);
    }

    public int height() {
        return height(root);
    }

    private int height(Node node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public int min() {
        if (root == null) {
            throw new NoSuchElementException("The registry is empty");
        }
        Node node = root;
        while (node.left != null) {
            node = node.left;
        }
        return node.bike;
    }

    public int max() {
        if (root == null) {
            throw new NoSuchElementException("The registry is empty");
        }
        Node node = root;
        while (node.right != null) {
            node = node.right;
        }
        return node.bike;
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
