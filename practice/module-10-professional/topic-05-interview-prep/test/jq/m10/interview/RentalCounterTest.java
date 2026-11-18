package jq.m10.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RentalCounterTest {

    @Test
    @DisplayName("A new counter counts 0, and every rental in the same second counts")
    void sameSecond() {
        RentalCounter counter = new RentalCounter();
        assertEquals(0, counter.countRecent(0), "Nothing was recorded yet, so the count is 0");
        counter.record(42);
        counter.record(42);
        counter.record(42);
        assertEquals(3, counter.countRecent(42), "Three rentals started at second 42; all three count");
        assertEquals(3, counter.countRecent(100), "At second 100, second 42 is still inside the last 300 seconds");
    }

    @Test
    @DisplayName("The window is the last 300 seconds: a rental at second 0 counts at 299 and is gone at 300")
    void windowEdges() {
        RentalCounter counter = new RentalCounter();
        counter.record(0);
        assertEquals(1, counter.countRecent(299),
                "The last 300 seconds at second 299 are seconds 0 to 299, so the rental at 0 counts");
        assertEquals(0, counter.countRecent(300),
                "The last 300 seconds at second 300 are seconds 1 to 300, so the rental at 0 is gone");
    }

    @Test
    @DisplayName("Old rentals drop out while newer ones stay, even when a bucket is reused")
    void oldRentalsDropOut() {
        RentalCounter counter = new RentalCounter();
        counter.record(5);
        counter.record(5);
        counter.record(5);
        counter.record(200);
        counter.record(305);
        assertEquals(2, counter.countRecent(305),
                "At second 305 the window is 6 to 305: the rentals at 200 and 305 count, the three at 5 do not. "
                        + "Second 305 lands in the same bucket as second 5, so reset that bucket first");
        counter.record(310);
        assertEquals(3, counter.countRecent(400), "At second 400 the window is 101 to 400: 200, 305 and 310 count");
        assertEquals(2, counter.countRecent(500), "At second 500 the window is 201 to 500: only 305 and 310 count");
        assertEquals(0, counter.countRecent(10_000), "Much later, every rental has dropped out");
    }

    @Test
    @DisplayName("A negative second, or a second earlier than one already seen, throws IllegalArgumentException")
    void timeNeverGoesBack() {
        RentalCounter counter = new RentalCounter();
        assertThrows(IllegalArgumentException.class, () -> counter.record(-1),
                "Seconds start at 0, so record(-1) must throw IllegalArgumentException");
        counter.record(50);
        assertThrows(IllegalArgumentException.class, () -> counter.record(49),
                "Second 49 comes after second 50 was already recorded: time went backwards, so throw");
        counter.countRecent(80);
        assertThrows(IllegalArgumentException.class, () -> counter.countRecent(79),
                "countRecent(80) was already asked, so countRecent(79) goes back in time and must throw");
        counter.record(80);
        assertEquals(2, counter.countRecent(80), "The same second again is fine: rentals at 50 and 80 count");
    }

    @Test
    @DisplayName("A million rentals with 100,000 counts in between finish in time")
    void millionRentalsAreFast() {
        RentalCounter counter = new RentalCounter();
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            for (int i = 0; i < 1_000_000; i++) {
                long second = i / 2;                       // two rentals every second
                counter.record(second);
                if (i % 10 == 0) {
                    long expected = (i + 1L) - 2L * Math.max(0, second - 299);
                    int actual = counter.countRecent(second);
                    if (actual != expected) {
                        assertEquals(expected, actual, "At second " + second + " (after rental number " + (i + 1)
                                + ") the last 300 seconds hold " + expected + " rentals");
                    }
                }
            }
        }, "Each record and each count should be O(1): 300 buckets at most. "
                + "Scanning every rental ever recorded is O(n) per count and times out");
    }
}
