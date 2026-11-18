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
        // TODO 1: throw IllegalArgumentException when virtualNodes < 1
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

    /** Puts the node's points hash(node + "#0") ... hash(node + "#" + (virtualNodes - 1)) on the ring. */
    public void addNode(String node) {
        // TODO 2: a node already on the ring throws IllegalArgumentException. Add it to nodes, then each point.
        //         If a point is already taken by another node, that node keeps it (putIfAbsent).
    }

    /** Takes the node's points off the ring. false if the node was not on it. */
    public boolean removeNode(String node) {
        // TODO 3: remove only the points this node owns: ring.remove(point, node)
        return false;
    }

    /** The node owning the first point at or after hash(key), wrapping round to the lowest point. */
    public String nodeFor(String key) {
        // TODO 4: an empty ring throws IllegalStateException. ceilingEntry finds the next point clockwise.
        return null;
    }

    /** How many points are on the ring. */
    public int pointCount() {
        // TODO 5
        return 0;
    }

    /** The nodes on the ring, in name order. */
    public List<String> nodes() {
        // TODO 6
        return List.of();
    }

    /** Each key with its node, in the keys' own order. */
    public Map<String, String> assign(Collection<String> keys) {
        Map<String, String> owners = new LinkedHashMap<>();
        // TODO 7: put each key with nodeFor(key)
        return owners;
    }

    /** How many keys of {@code before} have a different node in {@code after}. */
    public static int countMoved(Map<String, String> before, Map<String, String> after) {
        // TODO 8
        return 0;
    }
}
