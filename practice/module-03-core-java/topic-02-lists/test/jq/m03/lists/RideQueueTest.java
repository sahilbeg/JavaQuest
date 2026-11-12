package jq.m03.lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideQueueTest {

    private RideQueue sampleQueue() {
        RideQueue queue = new RideQueue();
        queue.add(new Ride("B-1042", "Harbor", "ebike", 25));
        queue.add(new Ride("B-2001", "Harbor", "classic", 1));
        queue.add(new Ride("B-3300", "Uptown", "cargo", 40));
        queue.add(new Ride("B-4100", "Riverside", "classic", 1));
        queue.add(new Ride("B-5000", "Harbor", "classic", 2));
        return queue;
    }

    @Test
    @DisplayName("addFirst puts a ride at the front of the queue")
    void addFirstGoesToFront() {
        RideQueue queue = sampleQueue();
        queue.addFirst(new Ride("B-7777", "Harbor", "classic", 12));
        assertEquals(6, queue.size(), "After addFirst the queue should hold 6 rides. Use rides.add(0, ride)");
        assertEquals("B-7777", queue.get(0).bikeId(), "get(0) should be the ride added with addFirst, B-7777");
        assertEquals("B-1042", queue.get(1).bikeId(), "The old first ride, B-1042, should move to position 1");
    }

    @Test
    @DisplayName("totalMinutes adds up every ride, and is 0 for an empty queue")
    void totalMinutesSums() {
        assertEquals(69, sampleQueue().totalMinutes(), "totalMinutes() should be 25 + 1 + 40 + 1 + 2 = 69");
        assertEquals(0, new RideQueue().totalMinutes(), "totalMinutes() on an empty queue should be 0");
    }

    @Test
    @DisplayName("removeShortRides removes rides under the limit and counts them")
    void removeShortRidesIsSafe() {
        RideQueue queue = sampleQueue();
        assertEquals(2, queue.removeShortRides(2),
                "removeShortRides(2) should remove the two 1-minute rides and return 2. A 2-minute ride stays");
        assertEquals(3, queue.size(), "3 rides should be left after removing 2 of 5");
        assertEquals("B-1042", queue.get(0).bikeId(), "B-1042 should still be first");
        assertEquals("B-3300", queue.get(1).bikeId(), "B-3300 should be second, after B-2001 is removed");
        assertEquals("B-5000", queue.get(2).bikeId(), "B-5000 (exactly 2 minutes) should stay, in third place");
    }

    @Test
    @DisplayName("ridesFrom returns a new list of rides from one station")
    void ridesFromFilters() {
        RideQueue queue = sampleQueue();
        List<Ride> harbor = queue.ridesFrom("Harbor");
        assertNotNull(harbor, "ridesFrom should return a new ArrayList, never null");
        assertEquals(3, harbor.size(), "There are 3 Harbor rides: B-1042, B-2001 and B-5000");
        assertEquals("B-1042", harbor.get(0).bikeId(), "Harbor rides should keep their original order");
        assertEquals("B-5000", harbor.get(2).bikeId(), "The last Harbor ride should be B-5000");
        assertEquals(0, queue.ridesFrom("Old Mill").size(), "ridesFrom(\"Old Mill\") should be an empty list");
        assertEquals(5, queue.size(), "ridesFrom must not change the queue itself");
    }

    @Test
    @DisplayName("longest returns the ride with the most minutes, or null when empty")
    void longestRide() {
        assertNull(new RideQueue().longest(), "longest() on an empty queue should return null");
        Ride longest = sampleQueue().longest();
        assertNotNull(longest, "longest() should return a ride when the queue has rides");
        assertEquals("B-3300", longest.bikeId(), "The longest ride is B-3300 at 40 minutes");
    }
}
