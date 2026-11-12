package jq.m04.linkedlists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiderListTest {

    @Test
    @DisplayName("addFirst and addLast put riders at the right ends")
    void addBothEnds() {
        RiderList<String> list = new RiderList<>();
        list.addLast("Ana");
        list.addLast("Ben");
        list.addFirst("Cleo");
        assertEquals("[Cleo, Ana, Ben]", list.toString(),
                "After addLast(Ana), addLast(Ben), addFirst(Cleo) the list should print [Cleo, Ana, Ben]");
        assertEquals(3, list.size(), "size() should be 3 after three adds");
        assertEquals("[]", new RiderList<String>().toString(), "An empty list should print []");
    }

    @Test
    @DisplayName("removeFirst hands back riders in order and null when empty")
    void removeFirst() {
        RiderList<String> list = new RiderList<>();
        list.addLast("Ana");
        list.addLast("Ben");
        assertEquals("Ana", list.removeFirst(), "removeFirst() should return the head item, Ana");
        assertEquals(1, list.size(), "size() should drop to 1 after one removeFirst()");
        assertEquals("Ben", list.removeFirst(), "The second removeFirst() should return Ben");
        assertNull(list.removeFirst(), "removeFirst() on an empty list should return null");
        assertEquals(0, list.size(), "size() should stay 0 after removing from an empty list");
    }

    @Test
    @DisplayName("get walks to the right node and rejects a bad index")
    void getByIndex() {
        RiderList<String> list = new RiderList<>();
        list.addLast("B-1042");
        list.addLast("B-2001");
        list.addLast("B-3310");
        assertEquals("B-1042", list.get(0), "get(0) should return the head item, B-1042");
        assertEquals("B-3310", list.get(2), "get(2) should return the third item, B-3310");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3),
                "get(3) on a list of 3 should throw IndexOutOfBoundsException");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1),
                "get(-1) should throw IndexOutOfBoundsException");
    }

    @Test
    @DisplayName("contains compares items with equals")
    void containsUsesEquals() {
        RiderList<String> list = new RiderList<>();
        list.addLast("Ana");
        list.addLast(new String("Ben"));
        assertTrue(list.contains(new String("Ben")),
                "contains(\"Ben\") should be true. Compare items with equals, because == compares references");
        assertFalse(list.contains("Zoe"), "contains(\"Zoe\") should be false: Zoe is not waiting");
    }

    @Test
    @DisplayName("reverse flips the list in place and adds still work afterwards")
    void reverseInPlace() {
        RiderList<Integer> list = new RiderList<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.reverse();
        assertEquals("[3, 2, 1]", list.toString(), "reverse() should turn [1, 2, 3] into [3, 2, 1]");
        list.addLast(0);
        assertEquals("[3, 2, 1, 0]", list.toString(),
                "After reverse(), addLast(0) should still reach the new end of the list");
        assertEquals(4, list.size(), "size() should be 4");
    }

    @Test
    @DisplayName("RiderList holds only a head node and a size counter")
    void onlyHeadAndSize() {
        String message = "RiderList should hold only head and size; build the chain of Node objects yourself instead of using a java.util list.";
        Map<String, Class<?>> fields = new TreeMap<>();
        for (Field field : RiderList.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                fields.put(field.getName(), field.getType());
            }
        }
        assertEquals(2, fields.size(), message);
        Class<?> headType = fields.get("head");
        assertTrue(headType != null && headType.getSimpleName().equals("Node")
                && headType.getEnclosingClass() == RiderList.class, message);
        assertEquals(int.class, fields.get("size"), message);
    }
}
