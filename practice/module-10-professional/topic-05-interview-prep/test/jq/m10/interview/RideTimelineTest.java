package jq.m10.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideTimelineTest {

    private static final String[] STATIONS = {"riverside", "old-mill", "harbor", "uptown", "depot"};

    private static RideTimeline b1042() {
        RideTimeline timeline = new RideTimeline();
        timeline.put("B-1042", 100, "riverside");
        timeline.put("B-1042", 400, "harbor");
        timeline.put("B-1042", 900, "uptown");
        return timeline;
    }

    @Test
    @DisplayName("get returns the latest sighting at or before the second asked about")
    void latestAtOrBefore() {
        RideTimeline timeline = b1042();
        assertEquals("riverside", timeline.get("B-1042", 100),
                "B-1042 was seen at riverside at second 100, so get(100) is riverside");
        assertEquals("riverside", timeline.get("B-1042", 250),
                "At second 250 the latest sighting is still riverside (second 100)");
        assertEquals("harbor", timeline.get("B-1042", 400),
                "A sighting at exactly second 400 counts: 'at or before' includes the second itself");
        assertEquals("harbor", timeline.get("B-1042", 899),
                "Second 899 is one before the uptown sighting, so the answer is still harbor");
        assertEquals("uptown", timeline.get("B-1042", 5_000),
                "After the last sighting, the answer is the last station: uptown");
    }

    @Test
    @DisplayName("get returns null before a bike's first sighting, and for a bike never seen")
    void nothingKnown() {
        RideTimeline timeline = b1042();
        assertNull(timeline.get("B-1042", 99),
                "B-1042 was first seen at second 100, so at second 99 there is no answer: return null");
        assertNull(timeline.get("B-1042", 0), "Second 0 is before every sighting: return null");
        assertNull(timeline.get("B-7", 500), "B-7 was never seen, so return null, not an exception");
        assertNull(new RideTimeline().get("B-1042", 100), "An empty timeline knows nothing: return null");
    }

    @Test
    @DisplayName("Each bike keeps its own sightings")
    void bikesAreSeparate() {
        RideTimeline timeline = b1042();
        timeline.put("B-7", 50, "depot");
        timeline.put("B-7", 700, "old-mill");
        assertEquals("depot", timeline.get("B-7", 650), "B-7 was at depot from second 50 until 700");
        assertEquals("old-mill", timeline.get("B-7", 700), "B-7 reached old-mill at second 700");
        assertEquals("harbor", timeline.get("B-1042", 700),
                "B-1042's answer must not change because B-7 moved: at second 700 it was at harbor");
        assertNull(timeline.get("B-1042", 60),
                "B-7's sighting at second 50 belongs to B-7 only. B-1042 has nothing before second 100");
    }

    @Test
    @DisplayName("put throws IllegalArgumentException when a bike's time does not go up, and changes nothing")
    void timesMustGoUp() {
        RideTimeline timeline = b1042();
        assertThrows(IllegalArgumentException.class, () -> timeline.put("B-1042", 900, "depot"),
                "B-1042 already has a sighting at second 900. A second one at 900 must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> timeline.put("B-1042", 300, "depot"),
                "Second 300 is before B-1042's last sighting (900), so put must throw IllegalArgumentException");
        assertEquals("harbor", timeline.get("B-1042", 450),
                "A refused put must not be stored: at second 450 B-1042 is still at harbor");
        assertEquals("uptown", timeline.get("B-1042", 900),
                "A refused put must not be stored: at second 900 B-1042 is at uptown, not depot");
        timeline.put("B-7", 300, "depot");
        assertEquals("depot", timeline.get("B-7", 300),
                "The rule is per bike: B-7 may have a sighting at second 300 even though B-1042 is at 900");
    }

    @Test
    @DisplayName("200,000 sightings and 200,000 lookups finish in time")
    void manyLookupsAreFast() {
        int n = 200_000;
        RideTimeline timeline = new RideTimeline();
        for (int i = 0; i < n; i++) {
            timeline.put("B-1042", 10L * i, STATIONS[i % STATIONS.length]);
        }
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            for (int i = 0; i < n; i++) {
                long second = 10L * i + 9;                 // between sighting i and sighting i + 1
                String expected = STATIONS[i % STATIONS.length];
                String actual = timeline.get("B-1042", second);
                if (!expected.equals(actual)) {
                    assertEquals(expected, actual, "At second " + second + " B-1042 was at sighting " + i);
                }
            }
        }, "200,000 lookups in a list of 200,000 should use binary search, O(log n) each. "
                + "Walking the list is O(n) each, 40 billion steps in all, and times out");
    }
}
