package jq.m04.triesunionfind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Station-name autocomplete for the app's search box, built as a trie (prefix tree).
 * Matching ignores upper and lower case: store each character with Character.toLowerCase.
 * A node where a whole name ends keeps that name, spelled the way it was first inserted.
 * Children sit in a TreeMap, so a depth-first walk meets the names in A to Z order.
 * Do not keep the names in a ready-made List, Set or Map field: use the nodes.
 */
public class StationTrie {

    private static class Node {
        final Map<Character, Node> children = new TreeMap<>();
        String name; // the station's spelling when a whole name ends here, otherwise null
    }

    private final Node root = new Node();
    private int size;

    /** Adds a station name. Returns true if it is new, false if it was already there (ignoring case). */
    public boolean insert(String name) {
        // TODO 1: walk down from root one lowercased character at a time, making a child Node where one is missing.
        // At the last node: if name is already set, return false. Otherwise set it, add 1 to size and return true.
        return false;
    }

    /** True only when this exact whole name was inserted, ignoring case. */
    public boolean contains(String name) {
        // TODO 2: follow the characters from root. Return false if a child is missing.
        // At the end, the name counts only if that node's name is not null.
        return false;
    }

    /** True when at least one stored name starts with the prefix, ignoring case. */
    public boolean startsWith(String prefix) {
        // TODO 3: follow the prefix like contains. The answer is true when you reach a node
        // that ends a name or still has children.
        return false;
    }

    /** Every stored name that starts with the prefix, in A to Z order ignoring case. */
    public List<String> complete(String prefix) {
        // TODO 4: walk to the prefix's node (empty list if it is missing). Then visit it and everything
        // below it, depth first: add the node's own name first, then visit its children in TreeMap order.
        return new ArrayList<>();
    }

    public int size() {
        return size;
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
