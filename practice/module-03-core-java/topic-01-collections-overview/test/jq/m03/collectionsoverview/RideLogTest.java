package jq.m03.collectionsoverview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideLogTest {

    @Test
    @DisplayName("A new ride log starts with zero rides")
    void newLogIsEmpty() {
        RideLog log = new RideLog();
        assertEquals(0, log.count(), "count() on a new RideLog should be 0. Start rides as an empty ArrayList");
    }

    @Test
    @DisplayName("Added rides are counted and kept in order")
    void addKeepsOrder() {
        RideLog log = new RideLog();
        log.add("B-1042");
        log.add("B-2001");
        log.add("B-1042");
        assertEquals(3, log.count(), "After 3 calls to add, count() should be 3. Duplicates are allowed in a list");
        assertEquals("B-1042", log.get(0), "get(0) should return the first bike id added, B-1042");
        assertEquals("B-2001", log.get(1), "get(1) should return the second bike id added, B-2001");
    }

    @Test
    @DisplayName("The log holds 101 rides without crashing")
    void growsPastOneHundred() {
        RideLog log = new RideLog();
        for (int i = 1; i <= 101; i++) {
            log.add("B-" + (1000 + i));
        }
        assertEquals(101, log.count(), "count() should be 101 after 101 rides. An ArrayList grows as you add");
        assertEquals("B-1101", log.get(100), "get(100) should return ride 101, bike B-1101");
    }

    @Test
    @DisplayName("pickCollection names the right collection for each job")
    void picksCollection() {
        assertEquals("List", RideLog.pickCollection("keep rides in order"),
                "\"keep rides in order\" needs order and duplicates, so return \"List\"");
        assertEquals("Set", RideLog.pickCollection("unique bike ids"),
                "\"unique bike ids\" needs each id once, so return \"Set\"");
        assertEquals("Map", RideLog.pickCollection("minutes per bike"),
                "\"minutes per bike\" looks up minutes by bike id, so return \"Map\"");
        assertEquals("", RideLog.pickCollection("paint the bikes"),
                "An unknown job should return \"\"");
    }
}
