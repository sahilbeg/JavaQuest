package jq.m04.hashtables;

public class DockTable {

    private static class Entry {
        final String key;
        Integer value;
        Entry next;

        Entry(String key, Integer value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Entry[] buckets = new Entry[8];
    private int size;

    public static int indexFor(String key, int buckets) {
        // TODO 1: return Math.floorMod(key.hashCode(), buckets)
        return -1;
    }

    public Integer get(String key) {
        // TODO 2: walk the chain at indexFor(key, buckets.length); return the value whose key equals key, or null
        return null;
    }

    public void put(String key, Integer value) {
        // TODO 3: replace the value if key is in the chain; otherwise add a new Entry and add 1 to size
        // TODO 4: then, if size > 0.75 * buckets.length, call resize()
    }

    public Integer remove(String key) {
        // TODO 5: unlink the entry for key, take 1 from size, and return its old value; return null if missing
        return null;
    }

    public int size() {
        // TODO 6: return how many keys are stored
        return -1;
    }

    public int bucketCount() {
        // TODO 6: return the length of the buckets array
        return -1;
    }

    private void resize() {
        // TODO 4: make an Entry array twice as long and move every entry to indexFor(key, newLength)
    }
}
