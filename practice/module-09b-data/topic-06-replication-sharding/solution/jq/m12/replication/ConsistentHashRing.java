package jq.m12.replication;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** A consistent-hash ring: each node owns many points (virtual nodes), and a key belongs to the next point clockwise. */
public final class ConsistentHashRing {

    private final int virtualNodes;
    private final TreeMap<Long, String> ring = new TreeMap<>();   // point on the ring -> node that owns it
    private final TreeSet<String> nodes = new TreeSet<>();

    public ConsistentHashRing(int virtualNodes) {
        if (virtualNodes < 1) {
            throw new IllegalArgumentException("each node needs at least 1 virtual node, got " + virtualNodes);
        }
        this.virtualNodes = virtualNodes;
    }

    /**
     * The ring's hash: 32-bit FNV-1a, then MurmurHash3's final mix, read as an unsigned number from 0 to 4,294,967,295.
     * The same text gives the same number on every machine. Given, finished.
     */
    public static long hash(String text) {
        int h = 0x811C9DC5;
        for (char c : text.toCharArray()) {
            h ^= c;
            h *= 0x01000193;
        }
        h ^= h >>> 16;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        h ^= h >>> 16;
        return Integer.toUnsignedLong(h);
    }

    public void addNode(String node) {
        if (!nodes.add(node)) {
            throw new IllegalArgumentException(node + " is already on the ring");
        }
        for (int i = 0; i < virtualNodes; i++) {
            ring.putIfAbsent(hash(node + "#" + i), node);
        }
    }

    public boolean removeNode(String node) {
        if (!nodes.remove(node)) {
            return false;
        }
        for (int i = 0; i < virtualNodes; i++) {
            ring.remove(hash(node + "#" + i), node);
        }
        return true;
    }

    public String nodeFor(String key) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("the ring has no nodes, so no node can own " + key);
        }
        Map.Entry<Long, String> next = ring.ceilingEntry(hash(key));
        return next != null ? next.getValue() : ring.firstEntry().getValue();
    }

    public int pointCount() {
        return ring.size();
    }

    public List<String> nodes() {
        return List.copyOf(nodes);
    }

    public Map<String, String> assign(Collection<String> keys) {
        Map<String, String> owners = new LinkedHashMap<>();
        for (String key : keys) {
            owners.put(key, nodeFor(key));
        }
        return owners;
    }

    public static int countMoved(Map<String, String> before, Map<String, String> after) {
        int moved = 0;
        for (Map.Entry<String, String> entry : before.entrySet()) {
            if (!entry.getValue().equals(after.get(entry.getKey()))) {
                moved++;
            }
        }
        return moved;
    }
}
