package jq.m12.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A B+ tree, the structure behind most database indexes. Every key and value lives in a leaf. Leaves are
 * linked left to right, so a range scan walks along them. Branch nodes hold only separator keys that guide
 * a search down. Each node stands for one disk page, and pageReads counts how many pages a lookup touched.
 */
public final class BPlusTree<K extends Comparable<K>, V> {

    /** One page. keys are sorted, with no repeats. */
    private abstract static class Node<K, V> {
        final List<K> keys = new ArrayList<>();
    }

    /** A leaf page: keys.get(i) belongs with values.get(i). next is the leaf to the right, or null. */
    private static final class Leaf<K, V> extends Node<K, V> {
        final List<V> values = new ArrayList<>();
        Leaf<K, V> next;
    }

    /** A branch page: children.size() is always keys.size() + 1. */
    private static final class Branch<K, V> extends Node<K, V> {
        final List<Node<K, V>> children = new ArrayList<>();
    }

    /** What a node hands to its parent after it split: the separator key and the new right-hand node. */
    private record Split<K, V>(K separator, Node<K, V> right) {}

    private final int maxKeys;
    private Node<K, V> root = new Leaf<>();
    private int height = 1;
    private int size;
    private long pageReads;

    public BPlusTree(int maxKeys) {
        if (maxKeys < 3) {
            throw new IllegalArgumentException("maxKeys must be at least 3, but was " + maxKeys);
        }
        this.maxKeys = maxKeys;
    }

    /** The value stored under key, or null. Reads exactly height() pages. */
    public V get(K key) {
        Objects.requireNonNull(key, "key");
        Leaf<K, V> leaf = findLeaf(key);
        int at = Collections.binarySearch(leaf.keys, key);
        return at >= 0 ? leaf.values.get(at) : null;
    }

    /** Stores value under key. A key already in the tree gets its value replaced, and size() does not change. */
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Split<K, V> split = insert(root, key, value);
        if (split != null) {
            Branch<K, V> newRoot = new Branch<>();
            newRoot.keys.add(split.separator());
            newRoot.children.add(root);
            newRoot.children.add(split.right());
            root = newRoot;
            height++;
        }
    }

    /** Every value whose key is between from and toInclusive, both included, in key order. */
    public List<V> range(K from, K toInclusive) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(toInclusive, "toInclusive");
        List<V> found = new ArrayList<>();
        if (from.compareTo(toInclusive) > 0) {
            return found;
        }
        Leaf<K, V> leaf = findLeaf(from);
        while (true) {
            for (int i = 0; i < leaf.keys.size(); i++) {
                K key = leaf.keys.get(i);
                if (key.compareTo(toInclusive) > 0) {
                    return found;
                }
                if (key.compareTo(from) >= 0) {
                    found.add(leaf.values.get(i));
                }
            }
            if (leaf.next == null) {
                return found;
            }
            leaf = read(leaf.next);
        }
    }

    public int height() {
        return height;
    }

    public int size() {
        return size;
    }

    public long pageReads() {
        return pageReads;
    }

    public void resetPageReads() {
        pageReads = 0;
    }

    /** The keys of every leaf, walking the leaf links from the leftmost leaf. Does not count page reads. */
    public List<List<K>> leafKeys() {
        Node<K, V> node = root;
        while (node instanceof Branch<K, V> branch) {
            node = branch.children.getFirst();
        }
        List<List<K>> leaves = new ArrayList<>();
        for (Leaf<K, V> leaf = (Leaf<K, V>) node; leaf != null; leaf = leaf.next) {
            leaves.add(List.copyOf(leaf.keys));
        }
        return leaves;
    }

    /** Counts one page read and hands the node back. Every node a search visits goes through here. */
    private <N extends Node<K, V>> N read(N node) {
        pageReads++;
        return node;
    }

    /** Which child of a branch to follow: the number of separator keys that are <= key. */
    private static <K extends Comparable<K>, V> int childIndex(Branch<K, V> branch, K key) {
        int at = Collections.binarySearch(branch.keys, key);
        return at >= 0 ? at + 1 : -(at + 1);
    }

    /** Walks from the root down to the leaf where key belongs, reading one page per level. */
    private Leaf<K, V> findLeaf(K key) {
        Node<K, V> node = read(root);
        while (node instanceof Branch<K, V> branch) {
            node = read(branch.children.get(childIndex(branch, key)));
        }
        return (Leaf<K, V>) node;
    }

    /** Puts key into the subtree under node. Returns the split this caused, or null. */
    private Split<K, V> insert(Node<K, V> node, K key, V value) {
        if (node instanceof Leaf<K, V> leaf) {
            int at = Collections.binarySearch(leaf.keys, key);
            if (at >= 0) {
                leaf.values.set(at, value);
                return null;
            }
            int slot = -(at + 1);
            leaf.keys.add(slot, key);
            leaf.values.add(slot, value);
            size++;
            return leaf.keys.size() > maxKeys ? splitLeaf(leaf) : null;
        }
        Branch<K, V> branch = (Branch<K, V>) node;
        int child = childIndex(branch, key);
        Split<K, V> below = insert(branch.children.get(child), key, value);
        if (below == null) {
            return null;
        }
        branch.keys.add(child, below.separator());
        branch.children.add(child + 1, below.right());
        return branch.keys.size() > maxKeys ? splitBranch(branch) : null;
    }

    /** The left leaf keeps the first (maxKeys + 1) / 2 entries. The right leaf's first key is copied up. */
    private Split<K, V> splitLeaf(Leaf<K, V> left) {
        int keep = (maxKeys + 1) / 2;
        Leaf<K, V> right = new Leaf<>();
        right.keys.addAll(left.keys.subList(keep, left.keys.size()));
        right.values.addAll(left.values.subList(keep, left.values.size()));
        left.keys.subList(keep, left.keys.size()).clear();
        left.values.subList(keep, left.values.size()).clear();
        right.next = left.next;
        left.next = right;
        return new Split<>(right.keys.getFirst(), right);
    }

    /** The key at (maxKeys + 1) / 2 moves up. Keys before it stay left, keys after it go right. */
    private Split<K, V> splitBranch(Branch<K, V> left) {
        int middle = (maxKeys + 1) / 2;
        K separator = left.keys.get(middle);
        Branch<K, V> right = new Branch<>();
        right.keys.addAll(left.keys.subList(middle + 1, left.keys.size()));
        right.children.addAll(left.children.subList(middle + 1, left.children.size()));
        left.keys.subList(middle, left.keys.size()).clear();
        left.children.subList(middle + 1, left.children.size()).clear();
        return new Split<>(separator, right);
    }
}
