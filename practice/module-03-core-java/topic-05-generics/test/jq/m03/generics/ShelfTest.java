package jq.m03.generics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShelfTest {

    @Test
    @DisplayName("A shelf of Strings keeps items in order and counts them")
    void shelfHoldsStrings() {
        Shelf<String> riders = new Shelf<>(3);
        assertTrue(riders.add("Ana"), "add should return true while the shelf has room");
        riders.add("Ben");
        assertEquals(2, riders.size(), "size() should be 2 after adding Ana and Ben");
        assertEquals("Ana", riders.get(0), "get(0) should return the first item you added");
        assertEquals("Ben", riders.get(1), "get(1) should return the second item you added");
    }

    @Test
    @DisplayName("A full shelf refuses new items")
    void fullShelfRefuses() {
        Shelf<Integer> docks = new Shelf<>(2);
        docks.add(1);
        docks.add(2);
        assertTrue(docks.isFull(), "isFull() should be true when size() reaches the capacity of 2");
        assertFalse(docks.add(3), "add should return false when the shelf is full");
        assertEquals(2, docks.size(), "size() should stay 2: a full shelf must not store the extra item");
    }

    @Test
    @DisplayName("A new shelf with room is not full")
    void newShelfNotFull() {
        Shelf<String> stations = new Shelf<>(12);
        assertTrue(stations.add("Harbor"), "add should return true on an empty 12-slot shelf");
        assertFalse(stations.isFull(), "isFull() should be false with 1 item on a 12-slot shelf");
    }

    @Test
    @DisplayName("Pair returns the two values it was given")
    void pairKeepsValues() {
        Pair<String, Integer> p = new Pair<>("B-1042", 25);
        assertEquals("B-1042", p.first(), "first() should return the first value passed to the constructor");
        assertEquals(25, p.second(), "second() should return the second value passed to the constructor");
    }

    @Test
    @DisplayName("lastOf and maxOf work for any type and return null for an empty list")
    void lastAndMax() {
        assertEquals("Uptown", GenericTools.lastOf(List.of("Harbor", "Uptown")),
                "lastOf should return the last item in the list");
        assertNull(GenericTools.lastOf(List.of()), "lastOf should return null for an empty list");
        assertEquals(45, GenericTools.maxOf(List.of(12, 45, 30)),
                "maxOf([12, 45, 30]) should be 45: use compareTo to find the biggest");
        assertEquals("Uptown", GenericTools.maxOf(List.of("Harbor", "Uptown", "Old Mill")),
                "maxOf of Strings should be Uptown, the last one alphabetically");
        assertNull(GenericTools.maxOf(List.<Integer>of()), "maxOf should return null for an empty list");
    }

    @Test
    @DisplayName("sum adds whole numbers and decimals")
    void sumNumbers() {
        assertEquals(40.0, GenericTools.sum(List.of(10, 25, 5)), 0.001,
                "sum([10, 25, 5]) should be 40.0: add n.doubleValue() for each number");
        assertEquals(4.0, GenericTools.sum(List.of(1.5, 2.5)), 0.001,
                "sum([1.5, 2.5]) should be 4.0");
    }
}
