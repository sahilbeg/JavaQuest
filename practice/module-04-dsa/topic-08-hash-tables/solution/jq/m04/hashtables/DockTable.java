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
        return Math.floorMod(key.hashCode(), buckets);
    }

    public Integer get(String key) {
        for (Entry e = buckets[indexFor(key, buckets.length)]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return null;
    }

    public void put(String key, Integer value) {
        int i = indexFor(key, buckets.length);
        for (Entry e = buckets[i]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }
        buckets[i] = new Entry(key, value, buckets[i]);
        size++;
        if (size > 0.75 * buckets.length) {
            resize();
        }
    }

    public Integer remove(String key) {
        int i = indexFor(key, buckets.length);
        Entry previous = null;
        for (Entry e = buckets[i]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                if (previous == null) {
                    buckets[i] = e.next;
                } else {
                    previous.next = e.next;
                }
                size--;
                return e.value;
            }
            previous = e;
        }
        return null;
    }

    public int size() {
        return size;
    }

    public int bucketCount() {
        return buckets.length;
    }

    private void resize() {
        Entry[] bigger = new Entry[buckets.length * 2];
        for (Entry head : buckets) {
            Entry e = head;
            while (e != null) {
                Entry following = e.next;
                int i = indexFor(e.key, bigger.length);
                e.next = bigger[i];
                bigger[i] = e;
                e = following;
            }
        }
        buckets = bigger;
    }
}
