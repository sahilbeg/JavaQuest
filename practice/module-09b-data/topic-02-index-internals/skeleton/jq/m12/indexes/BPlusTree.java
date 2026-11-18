package jq.m12.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A B+ tree, the structure behind most database indexes. Every key and value lives in a leaf. Leaves are
 * linked left to right, so a range scan walks along them. Branch nodes hold only separator keys that guide
 * a search down. Each node stands for one disk page, and pageReads counts how many pages a lookup touched.
 * The node classes, the counters, read(...) and leafKeys() are given. Work through the TODOs in number order.
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
        // TODO 1: throw IllegalArgumentException when maxKeys is below 3
        this.maxKeys = maxKeys;
    }

    /** The value stored under key, or null. Reads exactly height() pages. */
    public V get(K key) {
        Objects.requireNonNull(key, "key");
        // TODO 4: find the leaf with findLeaf(key), then look for key in leaf.keys (Collections.binarySearch helps).
        //         Found at index i: return leaf.values.get(i). Not found: return null.
        return null;
    }

    /** Stores value under key. A key already in the tree gets its value replaced, and size() does not change. */
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        // TODO 6: call insert(root, key, value). If it returns a split, the root itself split: make a new Branch
        //         with keys [split.separator()] and children [old root, split.right()], make it the root, and add 1 to height.
    }

    /** Every value whose key is between from and toInclusive, both included, in key order. */
    public List<V> range(K from, K toInclusive) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(toInclusive, "toInclusive");
        List<V> found = new ArrayList<>();
        // TODO 9: when from > toInclusive, return found at once, reading no page.
        //         Otherwise start at findLeaf(from). Scan the leaf's keys in order: stop and return when a key is > toInclusive,
        //         and add the value when the key is >= from. If the whole leaf was scanned, move to leaf.next with read(leaf.next),
        //         or return when next is null.
        return found;
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
        // TODO 2: return how many of branch.keys are <= key. A key equal to a separator goes to the RIGHT child.
        //         With keys [3, 5]: key 2 -> 0, key 3 -> 1, key 4 -> 1, key 5 -> 2, key 9 -> 2.
        return 0;
    }

    /** Walks from the root down to the leaf where key belongs, reading one page per level. */
    private Leaf<K, V> findLeaf(K key) {
        // TODO 3: start with read(root). While the node is a Branch, step into children.get(childIndex(branch, key)),
        //         passing every child through read(...) too. Return the leaf you reach. It reads exactly height() pages.
        return null;
    }

    /** Puts key into the subtree under node. Returns the split this caused, or null. */
    private Split<K, V> insert(Node<K, V> node, K key, V value) {
        // TODO 5: in a Leaf: if key is there, replace its value and return null. Otherwise add key and value at their
        //         sorted place, add 1 to size, and split the leaf if it now has more than maxKeys keys (return splitLeaf(leaf)).
        //         In a Branch: insert into the right child (childIndex). If the child split, add its separator at the child's
        //         index in keys and its right node just after the child in children; split this branch too if it has too many keys.
        return null;
    }

    /** The left leaf keeps the first (maxKeys + 1) / 2 entries. The right leaf's first key is copied up. */
    private Split<K, V> splitLeaf(Leaf<K, V> left) {
        // TODO 7: a new right leaf takes every entry from index (maxKeys + 1) / 2 on; the left leaf keeps the ones before.
        //         Link it in: right.next = left.next, then left.next = right. Return new Split<>(right's first key, right).
        return null;
    }

    /** The key at (maxKeys + 1) / 2 moves up. Keys before it stay left, keys after it go right. */
    private Split<K, V> splitBranch(Branch<K, V> left) {
        // TODO 8: middle = (maxKeys + 1) / 2. The key at middle moves UP (it stays in neither half).
        //         The right branch takes the keys after middle and the children from middle + 1 on; the left keeps the rest.
        //         Return new Split<>(the middle key, right).
        return null;
    }
}
