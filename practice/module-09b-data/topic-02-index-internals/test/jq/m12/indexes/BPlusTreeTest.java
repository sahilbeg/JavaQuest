package jq.m12.indexes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BPlusTreeTest {

    private static final Duration LIMIT = Duration.ofSeconds(5);

    /** A tree with maxKeys 4 holding the keys 1..n, put in ascending order, each with the value "v" + key. */
    private static BPlusTree<Integer, String> ascending(int n) {
        BPlusTree<Integer, String> tree = new BPlusTree<>(4);
        for (int key = 1; key <= n; key++) {
            tree.put(key, "v" + key);
        }
        return tree;
    }

    /** 3,000 random keys from 0 to 4,999 (some repeat), with a fixed seed so every run is the same. */
    private static void fillRandomly(BPlusTree<Integer, String> tree, TreeMap<Integer, String> expected) {
        Random random = new Random(1202);
        for (int i = 0; i < 3_000; i++) {
            int key = random.nextInt(5_000);
            String value = "ride-" + i;
            tree.put(key, value);
            expected.put(key, value);
        }
    }

    /**
     * The page reads the README's range rule gives: height() pages down to the leaf where from belongs,
     * then one more page for each further leaf the scan moves on to.
     */
    private static long expectedRangeReads(BPlusTree<Integer, String> tree, int from, int to) {
        List<List<Integer>> leaves = tree.leafKeys();
        int leaf = 0;
        for (int i = 0; i < leaves.size(); i++) {
            if (leaves.get(i).getFirst() <= from) {
                leaf = i;
            }
        }
        long reads = tree.height();
        while (leaves.get(leaf).getLast() <= to && leaf + 1 < leaves.size()) {
            leaf++;
            reads++;
        }
        return reads;
    }

    @Test
    @DisplayName("The constructor refuses fewer than 3 keys per node")
    void rejectsTinyNodes() {
        assertThrows(IllegalArgumentException.class, () -> new BPlusTree<Integer, String>(2),
                "new BPlusTree<>(2) should throw IllegalArgumentException: a node must hold at least 3 keys");
        assertThrows(IllegalArgumentException.class, () -> new BPlusTree<Integer, String>(0),
                "new BPlusTree<>(0) should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("An empty tree has height 1, finds nothing, and a lookup still reads the root page")
    void emptyTree() {
        BPlusTree<Integer, String> tree = new BPlusTree<>(4);

        assertNull(tree.get(1042), "get on an empty tree should return null");
        assertEquals(1, tree.pageReads(), "even an empty tree has one page, the root leaf, and get must read it");
        assertEquals(1, tree.height(), "an empty tree is one empty leaf, so height() is 1");
        assertEquals(0, tree.size(), "an empty tree holds 0 keys");
    }

    @Test
    @DisplayName("put stores values, get finds them, and putting a key again replaces its value")
    void putAndGet() {
        BPlusTree<String, String> tree = new BPlusTree<>(4);
        tree.put("rider-7", "Ada");
        tree.put("rider-3", "Ben");
        tree.put("rider-9", "Cy");
        tree.put("rider-7", "Ada Lovelace");

        assertEquals("Ada Lovelace", tree.get("rider-7"), "putting rider-7 again should replace its value");
        assertEquals("Ben", tree.get("rider-3"), "get(\"rider-3\") should find Ben");
        assertEquals("Cy", tree.get("rider-9"), "get(\"rider-9\") should find Cy");
        assertNull(tree.get("rider-1"), "a key that was never put should give null");
        assertEquals(3, tree.size(), "replacing a value must not count as a new key: 3 different keys were put");
    }

    @Test
    @DisplayName("The fifth key splits the root leaf: the left leaf keeps 2 keys and the tree grows to height 2")
    void firstSplit() {
        BPlusTree<Integer, String> tree = ascending(4);
        assertEquals(1, tree.height(), "4 keys fit in one leaf when maxKeys is 4, so height() is still 1");
        assertEquals(List.of(List.of(1, 2, 3, 4)), tree.leafKeys(), "4 keys fit in one leaf");

        tree.put(5, "v5");

        assertEquals(2, tree.height(), "the 5th key overflows the root leaf, so it splits and a new root is made: height 2");
        assertEquals(List.of(List.of(1, 2), List.of(3, 4, 5)), tree.leafKeys(),
                "a leaf with 5 keys splits so the left keeps (4 + 1) / 2 = 2 keys and the right gets the other 3, linked left to right");
        assertEquals("v3", tree.get(3), "after the split, get(3) should go right, because the separator 3 sends keys >= 3 right");
        assertEquals("v2", tree.get(2), "after the split, get(2) should go left");
    }

    @Test
    @DisplayName("Ascending keys split leaf after leaf, and the root splits at the 13th key")
    void rootSplitGrowsHeight() {
        assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5, 6, 7)), ascending(7).leafKeys(),
                "keys 1..7 with maxKeys 4 should end in three leaves: [1, 2], [3, 4], [5, 6, 7]");
        assertEquals(2, ascending(12).height(),
                "keys 1..12 make five leaves under one root with four separators, so height() is 2");
        BPlusTree<Integer, String> thirteen = ascending(13);
        assertEquals(3, thirteen.height(),
                "the 13th key makes a sixth leaf, so the root gets a fifth separator, overflows and splits: height 3");
        for (int key = 1; key <= 13; key++) {
            assertEquals("v" + key, thirteen.get(key), "after the root split, get(" + key + ") should still find v" + key);
        }
    }

    @Test
    @DisplayName("3,000 random puts give the same answers as a TreeMap")
    void matchesTreeMap() {
        assertTimeoutPreemptively(LIMIT, () -> {
            BPlusTree<Integer, String> tree = new BPlusTree<>(4);
            TreeMap<Integer, String> expected = new TreeMap<>();
            fillRandomly(tree, expected);

            assertEquals(expected.size(), tree.size(), "size() should count each different key once, like TreeMap.size()");
            for (int key = -5; key < 5_005; key++) {
                if (!java.util.Objects.equals(expected.get(key), tree.get(key))) {
                    fail("get(" + key + ") should be " + expected.get(key) + ", like the TreeMap, but was " + tree.get(key));
                }
            }
        }, "the random-put check took over 5 seconds; does a loop in put or get never end?");
    }

    @Test
    @DisplayName("The leaves hold every key once, in order, linked left to right, each between half full and full")
    void leavesAreLinkedAndHalfFull() {
        assertTimeoutPreemptively(LIMIT, () -> {
            BPlusTree<Integer, String> tree = new BPlusTree<>(4);
            TreeMap<Integer, String> expected = new TreeMap<>();
            fillRandomly(tree, expected);

            List<List<Integer>> leaves = tree.leafKeys();
            List<Integer> allKeys = new ArrayList<>();
            leaves.forEach(allKeys::addAll);
            assertEquals(new ArrayList<>(expected.keySet()), allKeys,
                    "walking the leaf links from the leftmost leaf should meet every key once, smallest first. "
                            + "Did a split forget right.next = left.next; left.next = right?");
            for (List<Integer> leaf : leaves) {
                assertTrue(leaf.size() >= 2 && leaf.size() <= 4,
                        "with maxKeys 4, every leaf of a split tree holds 2 to 4 keys, but one holds " + leaf);
            }
        }, "the leaf check took over 5 seconds; does a leaf's next link point back to itself?");
    }

    @Test
    @DisplayName("Every lookup, found or not, reads exactly height() pages")
    void lookupReadsOnePagePerLevel() {
        assertTimeoutPreemptively(LIMIT, () -> {
            BPlusTree<Integer, String> tree = new BPlusTree<>(4);
            fillRandomly(tree, new TreeMap<>());
            int height = tree.height();

            for (int key : new int[] {-1, 0, 7, 1202, 2500, 4999, 6000}) {
                tree.resetPageReads();
                tree.get(key);
                assertEquals(height, tree.pageReads(),
                        "get(" + key + ") should read one page per level, root to leaf: height() = " + height + " page(s). "
                                + "Call read(...) on the root and on every child you step into.");
            }
        }, "the lookup check took over 5 seconds");
    }

    @Test
    @DisplayName("The tree stays shallow: 2,000 keys need at most 8 levels with 4 keys per node")
    void staysShallow() {
        assertTimeoutPreemptively(LIMIT, () -> {
            BPlusTree<Integer, String> tree = new BPlusTree<>(4);
            for (int key = 0; key < 2_000; key++) {
                tree.put(key, "v" + key);
            }
            assertTrue(tree.height() <= 8,
                    "every leaf holds at least 2 keys and every branch at least 3 children, so 2,000 keys need at most "
                            + "1 + log3(1000), about 8 levels. Got height " + tree.height());
            assertTrue(tree.height() >= 5,
                    "a node holds at most 4 keys, so 2,000 keys cannot fit in fewer than 5 levels. Got height " + tree.height()
                            + ". Does height() go up by one each time the root splits?");
        }, "putting 2,000 keys took over 5 seconds");
    }

    @Test
    @DisplayName("A wide node keeps 100,000 keys only 3 levels deep")
    void wideNodesMeanFewLevels() {
        assertTimeoutPreemptively(LIMIT, () -> {
            BPlusTree<Integer, Integer> tree = new BPlusTree<>(100);
            for (int key = 0; key < 100_000; key++) {
                tree.put(key, key);
            }
            assertEquals(3, tree.height(), "with up to 100 keys per node, 100,000 keys fit in 3 levels");
            tree.resetPageReads();
            assertEquals(Integer.valueOf(76_543), tree.get(76_543), "get(76543) should find 76543");
            assertEquals(3, tree.pageReads(), "finding one key among 100,000 should read just 3 pages");
        }, "putting 100,000 keys took over 5 seconds; is put doing more than O(log n) work?");
    }

    @Test
    @DisplayName("range walks the leaf links: both ends included, keys in order, and from > to gives nothing")
    void rangeOnSmallTree() {
        BPlusTree<Integer, String> tree = ascending(7);   // leaves [1, 2] [3, 4] [5, 6, 7]

        tree.resetPageReads();
        assertEquals(List.of("v1", "v2", "v3", "v4", "v5", "v6", "v7"), tree.range(1, 7),
                "range(1, 7) should return every value, smallest key first, both ends included");
        assertEquals(4, tree.pageReads(),
                "range(1, 7): 2 pages down to leaf [1, 2], then 2 more leaves along the links, so 4 pages");

        tree.resetPageReads();
        assertEquals(List.of("v3", "v4"), tree.range(3, 4), "range(3, 4) should return v3 and v4");
        assertEquals(3, tree.pageReads(),
                "range(3, 4): 2 pages down to leaf [3, 4]. Its last key 4 is not past 4, so the scan reads the next leaf, "
                        + "sees 5 > 4 and stops: 3 pages");

        assertEquals(List.of("v4", "v5"), tree.range(4, 5), "range(4, 5) should cross from one leaf into the next");
        assertEquals(List.of(), tree.range(8, 9), "no key lies between 8 and 9, so range(8, 9) is empty");

        tree.resetPageReads();
        assertEquals(List.of(), tree.range(5, 2), "range(5, 2) has from > to, so it should be empty");
        assertEquals(0, tree.pageReads(), "when from > to, range should return at once without reading any page");
    }

    @Test
    @DisplayName("200 random ranges match TreeMap.subMap and read height() plus the leaves they move on to")
    void randomRanges() {
        assertTimeoutPreemptively(LIMIT, () -> {
            BPlusTree<Integer, String> tree = new BPlusTree<>(4);
            TreeMap<Integer, String> expected = new TreeMap<>();
            fillRandomly(tree, expected);
            Random random = new Random(7);

            for (int i = 0; i < 200; i++) {
                int from = random.nextInt(5_100) - 50;
                int to = from + random.nextInt(120);
                tree.resetPageReads();
                List<String> got = tree.range(from, to);
                assertEquals(new ArrayList<>(expected.subMap(from, true, to, true).values()), got,
                        "range(" + from + ", " + to + ") should match TreeMap.subMap(from, true, to, true)");
                assertEquals(expectedRangeReads(tree, from, to), tree.pageReads(),
                        "range(" + from + ", " + to + ") should read height() pages down to the leaf where " + from
                                + " belongs, then one page per further leaf, moving on only while a leaf's last key is <= " + to);
            }
        }, "the random range check took over 5 seconds; does the leaf walk ever stop?");
    }
}
