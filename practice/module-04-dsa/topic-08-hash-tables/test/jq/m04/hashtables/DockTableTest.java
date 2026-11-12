package jq.m04.hashtables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DockTableTest {

    @Test
    @DisplayName("indexFor uses Math.floorMod, so even a negative hashCode gives a safe index")
    void indexForUsesFloorMod() {
        assertEquals(3, DockTable.indexFor("Riverside", 8),
                "indexFor(\"Riverside\", 8) should be 3: floorMod(775688515, 8)");
        assertEquals(4, DockTable.indexFor("Harbor", 8),
                "indexFor(\"Harbor\", 8) should be 4. Harbor's hashCode is negative, so % gives -4; use Math.floorMod");
        assertEquals(12, DockTable.indexFor("Harbor", 16),
                "indexFor(\"Harbor\", 16) should be 12: the index depends on the bucket count");
    }

    @Test
    @DisplayName("put stores dock counts, get finds them, and put on the same key replaces the value")
    void putAndGet() {
        DockTable docks = new DockTable();
        docks.put("Riverside", 12);
        docks.put("Old Mill", 10);
        docks.put("Harbor", 9);
        assertEquals(12, docks.get("Riverside"), "get(\"Riverside\") should return 12, the value you put");
        assertEquals(9, docks.get("Harbor"), "get(\"Harbor\") should return 9");
        assertNull(docks.get("Uptown"), "get(\"Uptown\") should return null: Uptown was never put");
        assertEquals(3, docks.size(), "Three different stations were put, so size() should be 3");
        docks.put("Harbor", 4);
        assertEquals(4, docks.get("Harbor"), "Putting Harbor again should replace 9 with 4");
        assertEquals(3, docks.size(), "Replacing a value must not add a new key: size() should still be 3");
        assertEquals(10, docks.get(new String("Old Mill")),
                "get should find Old Mill even when the key is a different String object. Compare keys with equals");
    }

    @Test
    @DisplayName("Keys with the same hashCode share a bucket and both stay findable")
    void collisionsChain() {
        assertEquals("Aa".hashCode(), "BB".hashCode(), "Aa and BB have the same hashCode, 2112");
        DockTable docks = new DockTable();
        docks.put("Aa", 1);
        docks.put("BB", 2);
        assertEquals(1, docks.get("Aa"), "get(\"Aa\") should be 1. Aa and BB share a bucket: keep both in the chain");
        assertEquals(2, docks.get("BB"), "get(\"BB\") should be 2. Walk the chain and compare keys with equals");
        assertEquals(2, docks.size(), "Aa and BB are different keys, so size() should be 2");
        assertEquals(1, docks.remove("Aa"), "remove(\"Aa\") should return its old value, 1");
        assertNull(docks.get("Aa"), "After remove(\"Aa\"), get(\"Aa\") should be null");
        assertEquals(2, docks.get("BB"), "Removing Aa must not lose BB from the same chain");
    }

    @Test
    @DisplayName("remove returns the old value, or null for a missing key")
    void removeWorks() {
        DockTable docks = new DockTable();
        docks.put("Uptown", 7);
        docks.put("Old Mill", 10);
        assertEquals(7, docks.remove("Uptown"), "remove(\"Uptown\") should return the old value 7");
        assertNull(docks.remove("Uptown"), "Removing Uptown a second time should return null");
        assertNull(docks.remove("Depot"), "remove(\"Depot\") should return null: Depot was never put");
        assertEquals(1, docks.size(), "After removing Uptown, size() should be 1");
        assertEquals(10, docks.get("Old Mill"), "Old Mill should still be there with 10");
    }

    @Test
    @DisplayName("The table doubles its buckets when size passes 0.75 of the bucket count, and keeps every key")
    void resizes() {
        DockTable docks = new DockTable();
        assertEquals(8, docks.bucketCount(), "A new DockTable should start with 8 buckets");
        for (int i = 1; i <= 6; i++) {
            docks.put("B-10" + i, i);
        }
        assertEquals(8, docks.bucketCount(), "6 keys is exactly 0.75 * 8, so the table should still have 8 buckets");
        docks.put("B-107", 7);
        assertEquals(16, docks.bucketCount(), "7 keys is more than 0.75 * 8 = 6, so the table should double to 16 buckets");
        for (int i = 8; i <= 13; i++) {
            docks.put("B-10" + i, i);
        }
        assertEquals(32, docks.bucketCount(), "13 keys is more than 0.75 * 16 = 12, so the table should double to 32 buckets");
        assertEquals(13, docks.size(), "13 different keys were put, so size() should be 13");
        for (int i = 1; i <= 13; i++) {
            assertEquals(i, docks.get("B-10" + i),
                    "get(\"B-10" + i + "\") should still be " + i + " after resizing. Rehash every entry at its new index");
        }
    }

    @Test
    @DisplayName("DockTable is built by hand, with no java.util map or list inside")
    void builtByHand() {
        for (Field field : DockTable.class.getDeclaredFields()) {
            assertFalse(field.getType().getName().startsWith("java.util"),
                    "DockTable should not use " + field.getType().getSimpleName() + ". Use the Entry[] buckets array");
        }
    }
}
