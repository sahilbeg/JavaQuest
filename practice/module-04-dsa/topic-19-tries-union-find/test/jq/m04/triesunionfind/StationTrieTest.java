package jq.m04.triesunionfind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StationTrieTest {

    private static StationTrie trieOf(String... names) {
        StationTrie trie = new StationTrie();
        for (String name : names) {
            trie.insert(name);
        }
        return trie;
    }

    @Test
    @DisplayName("insert returns true for a new name and false for a name already there, in any case")
    void insertCountsNewNamesOnly() {
        StationTrie trie = new StationTrie();
        assertTrue(trie.insert("Harbor"), "insert(\"Harbor\") on an empty trie should return true: it is a new name");
        assertTrue(trie.insert("Uptown"), "insert(\"Uptown\") should return true: it is a new name");
        assertFalse(trie.insert("Harbor"), "insert(\"Harbor\") a second time should return false");
        assertFalse(trie.insert("HARBOR"), "insert(\"HARBOR\") should return false: matching ignores case");
        assertEquals(2, trie.size(), "size() should be 2: only Harbor and Uptown are new");
    }

    @Test
    @DisplayName("contains is true only for whole names, never for a prefix of a name")
    void containsNeedsTheWholeName() {
        StationTrie trie = trieOf("Riverside", "Old Mill");
        assertTrue(trie.contains("Riverside"), "contains(\"Riverside\") should be true: it was inserted");
        assertTrue(trie.contains("old mill"), "contains(\"old mill\") should be true: matching ignores case");
        assertFalse(trie.contains("River"), "contains(\"River\") should be false: River is only the start of Riverside. "
                + "A name ends only where you marked it");
        assertFalse(trie.contains("Riversides"), "contains(\"Riversides\") should be false: the path runs out of letters");
        assertFalse(new StationTrie().contains("Depot"), "contains on an empty trie should be false");
    }

    @Test
    @DisplayName("startsWith is true when some stored name begins with the prefix")
    void startsWithChecksPrefixes() {
        StationTrie trie = trieOf("Riverside", "Uptown");
        assertTrue(trie.startsWith("Riv"), "startsWith(\"Riv\") should be true: Riverside starts with Riv");
        assertTrue(trie.startsWith("up"), "startsWith(\"up\") should be true: matching ignores case");
        assertTrue(trie.startsWith("Uptown"), "startsWith(\"Uptown\") should be true: a whole name is a prefix of itself");
        assertFalse(trie.startsWith("Riw"), "startsWith(\"Riw\") should be false: no name starts with Riw");
        assertFalse(trie.startsWith("Uptownx"), "startsWith(\"Uptownx\") should be false: it is longer than any name");
    }

    @Test
    @DisplayName("complete lists every matching name A to Z, in the spelling it was inserted with")
    void completeIsSorted() {
        StationTrie trie = trieOf("Riverside", "Old Mill", "Harbor", "River Park", "Uptown", "Rivet Works");
        assertEquals(List.of("River Park", "Riverside", "Rivet Works"), trie.complete("riv"),
                "complete(\"riv\") should list the three names that start with riv, A to Z. "
                        + "A space comes before letters, so River Park is first");
        assertEquals(List.of("Old Mill"), trie.complete("OLD"), "complete(\"OLD\") should find Old Mill: matching ignores case");
        assertEquals(List.of(), trie.complete("Depot"), "complete(\"Depot\") should be an empty list: no name starts with Depot");
    }

    @Test
    @DisplayName("complete includes a name that is itself the start of a longer name")
    void completeIncludesShortNames() {
        StationTrie trie = trieOf("Harbor East", "Harbor");
        assertFalse(new StationTrie().startsWith(""), "startsWith(\"\") on an empty trie should be false: there are no names");
        assertEquals(List.of("Harbor", "Harbor East"), trie.complete("Harb"),
                "complete(\"Harb\") should list Harbor before Harbor East. Collect a node's own name before its children");
        assertEquals(List.of("Harbor", "Harbor East"), trie.complete(""),
                "complete(\"\") should list every name: the empty prefix matches all of them");
    }

    @Test
    @DisplayName("StationTrie keeps names in its own nodes, not in a ready-made set or list")
    void builtByHand() {
        assertEquals(List.of("Depot"), trieOf("Depot").complete("d"), "complete(\"d\") should find Depot");
        for (Field field : StationTrie.class.getDeclaredFields()) {
            assertFalse(Collection.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType()),
                    "StationTrie should not keep a " + field.getType().getSimpleName()
                            + " field. Store the names in the trie's own Node objects");
        }
    }
}
