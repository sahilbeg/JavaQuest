package jq.m12.indexes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import jq.m12.indexes.IndexAdvisor.IndexUse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IndexAdvisorTest {

    @Test
    @DisplayName("Equality column first, range column last: the index uses both columns")
    void equalityThenRange() {
        IndexUse use = IndexAdvisor.evaluate(List.of("bike_id", "started_at"), Set.of("bike_id"), "started_at");

        assertEquals(new IndexUse(true, 2), use,
                "WHERE bike_id = ? AND started_at BETWEEN ? AND ? can jump to the bike, then scan just its time range "
                        + "in index (bike_id, started_at): both columns are used");
    }

    @Test
    @DisplayName("A range on the first column stops the prefix: the columns after it are not used")
    void rangeFirstStops() {
        IndexUse use = IndexAdvisor.evaluate(List.of("started_at", "bike_id"), Set.of("bike_id"), "started_at");

        assertEquals(new IndexUse(true, 1), use,
                "in index (started_at, bike_id), the range on started_at is used, but after a range the bike ids "
                        + "are no longer in order, so bike_id cannot narrow the search: 1 column used");
    }

    @Test
    @DisplayName("An index whose first column the query does not test cannot be used, but the right order can")
    void leftmostColumnMissing() {
        assertEquals(new IndexUse(false, 0),
                IndexAdvisor.evaluate(List.of("rider_id", "started_at"), Set.of("bike_id"), "started_at"),
                "the query never tests rider_id, the first column of (rider_id, started_at), so the index cannot be used at all");
        assertEquals(new IndexUse(true, 1),
                IndexAdvisor.evaluate(List.of("bike_id", "rider_id"), Set.of("bike_id"), null),
                "(bike_id, rider_id) with WHERE bike_id = ? uses its first column only. A null range column means no range.");
    }

    @Test
    @DisplayName("A column the query skips ends the prefix, even if a later column is tested")
    void gapStopsThePrefix() {
        IndexUse use = IndexAdvisor.evaluate(List.of("bike_id", "rider_id", "started_at"), Set.of("bike_id"), "started_at");

        assertEquals(new IndexUse(true, 1), use,
                "in (bike_id, rider_id, started_at), the query skips rider_id, so started_at is out of order within a bike: "
                        + "only bike_id is used");
    }

    @Test
    @DisplayName("Several equality columns are used in any order, and names are compared ignoring case")
    void equalityColumnsAnyOrderAnyCase() {
        assertEquals(new IndexUse(true, 3),
                IndexAdvisor.evaluate(List.of("BIKE_ID", "Rider_Id", "started_at"), Set.of("rider_id", "bike_id"), "STARTED_AT"),
                "bike_id and rider_id are both tested with =, so both lead, and the range on started_at comes after: 3 columns. "
                        + "Compare names ignoring case.");
        assertEquals(new IndexUse(true, 2),
                IndexAdvisor.evaluate(List.of("rider_id", "bike_id"), Set.of("bike_id", "rider_id", "station_id"), null),
                "every index column is tested with =, so both are used. station_id is not in the index and does not matter.");
    }

    @Test
    @DisplayName("evaluate rejects an empty index and a column that is both an equality and the range")
    void badInput() {
        assertThrows(IllegalArgumentException.class,
                () -> IndexAdvisor.evaluate(List.of(), Set.of("bike_id"), null),
                "an index with no columns should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> IndexAdvisor.evaluate(List.of("bike_id"), Set.of("started_at"), "started_at"),
                "started_at cannot be tested with = and as a range at once, so evaluate should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("suggest puts the equality columns first, sorted, then the range column; bestIndex picks the most used columns")
    void suggestAndBestIndex() {
        assertEquals(List.of("bike_id", "rider_id", "started_at"),
                IndexAdvisor.suggest(Set.of("bike_id", "Rider_Id"), "started_at"),
                "suggest should list the equality columns in lower case and alphabetical order, then the range column last");
        assertEquals(List.of("started_at"), IndexAdvisor.suggest(Set.of(), "started_at"),
                "with no equality columns, the suggested index is just the range column");

        List<List<String>> indexes = List.of(
                List.of("rider_id"),
                List.of("started_at", "bike_id"),
                List.of("bike_id", "started_at"),
                List.of("bike_id", "started_at", "rider_id"));
        assertEquals(2, IndexAdvisor.bestIndex(indexes, Set.of("bike_id"), "started_at"),
                "index 2 (bike_id, started_at) and index 3 both use 2 columns; the earlier one, index 2, wins the tie");
        assertEquals(-1, IndexAdvisor.bestIndex(indexes, Set.of("station_id"), null),
                "no index starts with station_id, so none is usable and bestIndex should return -1");
    }
}
