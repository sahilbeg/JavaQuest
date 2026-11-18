package jq.m12.replication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsistentHashRingTest {

    private static List<String> riders(int count) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add("rider-" + i);
        }
        return keys;
    }

    private static ConsistentHashRing ring(int virtualNodes, String... nodes) {
        ConsistentHashRing ring = new ConsistentHashRing(virtualNodes);
        for (String node : nodes) {
            ring.addNode(node);
        }
        return ring;
    }

    @Test
    @DisplayName("An empty ring cannot place a key, and a node needs at least one virtual node")
    void emptyRingAndBadArguments() {
        assertThrows(IllegalArgumentException.class, () -> new ConsistentHashRing(0),
                "0 virtual nodes would put a node nowhere on the ring: throw IllegalArgumentException");
        ConsistentHashRing empty = new ConsistentHashRing(10);
        assertThrows(IllegalStateException.class, () -> empty.nodeFor("rider-7"),
                "with no nodes on the ring, nodeFor should throw IllegalStateException");
    }

    @Test
    @DisplayName("Each node puts V points on the ring, and one node alone owns every key")
    void pointsAndSingleNode() {
        ConsistentHashRing ring = ring(50, "shard-a");
        assertEquals(50, ring.pointCount(), "one node with 50 virtual nodes should put 50 points on the ring");
        for (String key : riders(200)) {
            assertEquals("shard-a", ring.nodeFor(key), "with one node, " + key + " can only belong to shard-a");
        }
        ring.addNode("shard-b");
        ring.addNode("shard-c");
        assertEquals(150, ring.pointCount(), "three nodes with 50 virtual nodes each should make 150 points");
        assertEquals(List.of("shard-a", "shard-b", "shard-c"), ring.nodes(), "nodes() should list the nodes in name order");
    }

    @Test
    @DisplayName("Keys land on the exact nodes the ring rule gives, with points named node#0 to node#49")
    void exactOwners() {
        ConsistentHashRing ring = ring(50, "shard-a", "shard-b", "shard-c");
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("rider-7", "shard-b");
        expected.put("rider-8", "shard-a");
        expected.put("rider-42", "shard-a");
        expected.put("rider-1042", "shard-c");
        expected.put("rider-99999", "shard-c");
        expected.forEach((key, node) -> assertEquals(node, ring.nodeFor(key),
                key + " should belong to " + node + ": hash the key, then take the first point at or after it,"
                        + " where the points are hash(\"shard-a#0\") ... hash(\"shard-c#49\")"));
        assertEquals(expected, ring.assign(expected.keySet()), "assign should map every key to the same node as nodeFor");
    }

    @Test
    @DisplayName("A key past the last point on the ring wraps round to the first point")
    void wrapsAround() {
        ConsistentHashRing ring = ring(1, "shard-a", "shard-b", "shard-c");
        Map<Long, String> pointOwner = new HashMap<>();
        for (String node : List.of("shard-a", "shard-b", "shard-c")) {
            pointOwner.put(ConsistentHashRing.hash(node + "#0"), node);
        }
        long highest = pointOwner.keySet().stream().max(Long::compare).orElseThrow();
        long lowest = pointOwner.keySet().stream().min(Long::compare).orElseThrow();

        String pastTheEnd = null;
        for (int i = 0; pastTheEnd == null; i++) {
            if (ConsistentHashRing.hash("rider-" + i) > highest) {
                pastTheEnd = "rider-" + i;
            }
        }
        assertEquals(pointOwner.get(lowest), ring.nodeFor(pastTheEnd), pastTheEnd
                + " hashes past every point, so it should wrap round to the owner of the lowest point");
    }

    @Test
    @DisplayName("Adding a fifth node moves about a fifth of the keys, and every moved key goes to the new node")
    void addingANodeMovesFewKeys() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            List<String> keys = riders(10_000);
            ConsistentHashRing ring = ring(100, "shard-a", "shard-b", "shard-c", "shard-d");
            Map<String, String> before = ring.assign(keys);

            ring.addNode("shard-e");
            Map<String, String> after = ring.assign(keys);

            int moved = ConsistentHashRing.countMoved(before, after);
            assertEquals(2_149, moved, "with these exact nodes and keys, 2,149 of 10,000 keys should change owner");
            for (String key : keys) {
                if (!before.get(key).equals(after.get(key))) {
                    assertEquals("shard-e", after.get(key), key + " moved, so it must have moved to the new node, shard-e");
                }
            }
        }, "the ring should place 20,000 keys well within 2 s: use a TreeMap and ceilingEntry, not a loop over every point");
    }

    @Test
    @DisplayName("Removing a node moves only that node's keys, and an unknown node or a duplicate is refused")
    void removingANode() {
        List<String> keys = riders(2_000);
        ConsistentHashRing ring = ring(100, "shard-a", "shard-b", "shard-c", "shard-d");
        Map<String, String> before = ring.assign(keys);

        assertTrue(ring.removeNode("shard-b"), "removing a node on the ring should answer true");
        assertEquals(300, ring.pointCount(), "after removing shard-b, 3 nodes × 100 points should be left");
        Map<String, String> after = ring.assign(keys);
        for (String key : keys) {
            if (!before.get(key).equals("shard-b")) {
                assertEquals(before.get(key), after.get(key), key + " was not on shard-b, so it must not move");
            } else {
                assertFalse(after.get(key).equals("shard-b"), key + " was on shard-b, which is gone, so it must move");
            }
        }
        assertFalse(ring.removeNode("shard-b"), "removing a node that is not on the ring should answer false");
        assertThrows(IllegalArgumentException.class, () -> ring.addNode("shard-a"),
                "adding a node that is already on the ring should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("countMoved counts the keys whose owner differs between two assignments")
    void countMoved() {
        Map<String, String> before = new LinkedHashMap<>();
        before.put("rider-1", "shard-a");
        before.put("rider-2", "shard-b");
        before.put("rider-3", "shard-a");
        Map<String, String> after = new LinkedHashMap<>(before);
        assertEquals(0, ConsistentHashRing.countMoved(before, after), "identical assignments: nothing moved");
        after.put("rider-3", "shard-c");
        after.put("rider-2", "shard-c");
        assertEquals(2, ConsistentHashRing.countMoved(before, after), "rider-2 and rider-3 changed owner: 2 moved");
    }

    @Test
    @DisplayName("With 100 virtual nodes each, four nodes get a fair share of 10,000 keys")
    void virtualNodesBalance() {
        Map<String, Integer> counts = new HashMap<>();
        ring(100, "shard-a", "shard-b", "shard-c", "shard-d").assign(riders(10_000))
                .values().forEach(node -> counts.merge(node, 1, Integer::sum));
        assertEquals(4, counts.size(), "all four nodes should own some keys");
        counts.forEach((node, count) -> assertTrue(count >= 2_000 && count <= 3_000,
                node + " owns " + count + " keys; with 100 virtual nodes each node should own between 2,000 and 3,000"));
    }
}
