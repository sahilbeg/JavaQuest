package jq.m04.triesunionfind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Station-name autocomplete for the app's search box, built as a trie (prefix tree).
 * Matching ignores upper and lower case: each character is stored with Character.toLowerCase.
 * A node where a whole name ends keeps that name, spelled the way it was first inserted.
 * Children sit in a TreeMap, so a depth-first walk meets the names in A to Z order.
 */
public class StationTrie {

    private static class Node {
        final Map<Character, Node> children = new TreeMap<>();
        String name;
    }

    private final Node root = new Node();
    private int size;

    /** Adds a station name. Returns true if it is new, false if it was already there (ignoring case). */
    public boolean insert(String name) {
        Node node = root;
        for (int i = 0; i < name.length(); i++) {
            char c = Character.toLowerCase(name.charAt(i));
            node = node.children.computeIfAbsent(c, key -> new Node());
        }
        if (node.name != null) {
            return false;
        }
        node.name = name;
        size++;
        return true;
    }

    /** True only when this exact whole name was inserted, ignoring case. */
    public boolean contains(String name) {
        Node node = walk(name);
        return node != null && node.name != null;
    }

    /** True when at least one stored name starts with the prefix, ignoring case. */
    public boolean startsWith(String prefix) {
        Node node = walk(prefix);
        return node != null && (node.name != null || !node.children.isEmpty());
    }

    /** Every stored name that starts with the prefix, in A to Z order ignoring case. */
    public List<String> complete(String prefix) {
        List<String> names = new ArrayList<>();
        Node node = walk(prefix);
        if (node != null) {
            collect(node, names);
        }
        return names;
    }

    public int size() {
        return size;
    }

    private Node walk(String text) {
        Node node = root;
        for (int i = 0; i < text.length() && node != null; i++) {
            node = node.children.get(Character.toLowerCase(text.charAt(i)));
        }
        return node;
    }

    private void collect(Node node, List<String> names) {
        if (node.name != null) {
            names.add(node.name);
        }
        for (Node child : node.children.values()) {
            collect(child, names);
        }
    }

    public static void main(String[] args) {
        StationTrie trie = new StationTrie();
        for (String station : List.of("Riverside", "Old Mill", "Harbor", "Uptown", "Depot", "Harbor East", "River Park")) {
            trie.insert(station);
        }
        System.out.println("riv -> " + trie.complete("riv"));
        System.out.println("har -> " + trie.complete("har"));
        System.out.println("Has 'Harb' as a whole name? " + trie.contains("Harb"));
        System.out.println("Any name starting with 'Harb'? " + trie.startsWith("Harb"));
    }
}
