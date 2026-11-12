package jq.m03.sets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeUsageTest {

    @Test
    @DisplayName("distinctBikes counts a bike ridden three times only once")
    void distinctCountsEachBikeOnce() {
        List<String> rides = List.of("B-1042", "B-2001", "B-1042", "B-1042", "B-3300");
        assertEquals(3, BikeUsage.distinctBikes(rides),
                "distinctBikes should be 3: B-1042, B-2001 and B-3300. B-1042 rode three times but is one bike");
        assertEquals(0, BikeUsage.distinctBikes(List.of()),
                "distinctBikes of an empty list should be 0");
    }

    @Test
    @DisplayName("sortedIds returns each id once in alphabetical order")
    void sortedIdsAreSortedAndUnique() {
        List<String> rides = List.of("B-3300", "B-1042", "B-2001", "B-1042");
        assertEquals(List.of("B-1042", "B-2001", "B-3300"), BikeUsage.sortedIds(rides),
                "sortedIds should drop the repeated B-1042 and sort the ids. A TreeSet does both");
    }

    @Test
    @DisplayName("usedAtBoth returns the sorted ids that appear at both stations")
    void usedAtBothIsTheIntersection() {
        List<String> harbor = List.of("B-3300", "B-1042", "B-2001", "B-1042");
        List<String> uptown = List.of("B-7777", "B-3300", "B-1042");
        assertEquals(List.of("B-1042", "B-3300"), BikeUsage.usedAtBoth(harbor, uptown),
                "usedAtBoth should be [B-1042, B-3300]: only those two are in both lists. Use retainAll on a TreeSet copy");
    }

    @Test
    @DisplayName("usedAtBoth returns an empty list when no bike is shared")
    void usedAtBothCanBeEmpty() {
        assertEquals(List.of(), BikeUsage.usedAtBoth(List.of("B-1"), List.of("B-2")),
                "usedAtBoth should be an empty list when the stations share no bike");
    }

    @Test
    @DisplayName("firstDuplicate returns the first id that shows up a second time")
    void firstDuplicateFindsTheFirstRepeat() {
        List<String> rides = List.of("B-2001", "B-1042", "B-3300", "B-1042", "B-2001");
        assertEquals("B-1042", BikeUsage.firstDuplicate(rides),
                "firstDuplicate should be B-1042: it repeats as the fourth ride, before B-2001 repeats as the fifth ride");
    }

    @Test
    @DisplayName("firstDuplicate returns null when every id is different")
    void firstDuplicateNullWhenNoRepeat() {
        assertNull(BikeUsage.firstDuplicate(List.of("B-1", "B-2", "B-3")),
                "firstDuplicate should return null when no id repeats");
    }
}
