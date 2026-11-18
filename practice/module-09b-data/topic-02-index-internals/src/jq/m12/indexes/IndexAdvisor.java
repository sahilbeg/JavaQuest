package jq.m12.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Answers one question a query planner asks: can this composite index help a query, and how many of its
 * columns can it use? It follows the leftmost-prefix rule. Column names are compared ignoring case.
 */
public final class IndexAdvisor {

    /** usable is true when columnsUsed is at least 1. */
    public record IndexUse(boolean usable, int columnsUsed) {}

    private IndexAdvisor() {}

    /**
     * How much of an index a query can use. The query tests each column in equalityColumns with =, and
     * rangeColumn (or null for none) with <, >, BETWEEN or similar.
     */
    public static IndexUse evaluate(List<String> indexColumns, Set<String> equalityColumns, String rangeColumn) {
        // TODO 10: throw IllegalArgumentException when indexColumns is null or empty, or when rangeColumn is also
        //          one of the equalityColumns. Compare every name ignoring case (lower(...) and toLowerCase(Locale.ROOT) help).
        // TODO 11: walk indexColumns from the left. An equality column: count it and go on. The range column: count it
        //          and stop. Any other column: stop. Return new IndexUse(count > 0, count).
        return new IndexUse(false, 0);
    }

    /** The index to build for a query: its equality columns in alphabetical order, then the range column. */
    public static List<String> suggest(Set<String> equalityColumns, String rangeColumn) {
        Objects.requireNonNull(equalityColumns, "equalityColumns");
        // TODO 12: the equality columns in lower case and alphabetical order, then the range column in lower case
        //          (if it is not null). Throw IllegalArgumentException if the range column is also an equality column.
        return new ArrayList<>();
    }

    /** The position of the index that uses the most columns. The earliest one wins a tie. -1 when none is usable. */
    public static int bestIndex(List<List<String>> indexes, Set<String> equalityColumns, String rangeColumn) {
        // TODO 13: evaluate each index in turn and keep the position of the one with the most columnsUsed.
        //          Only a strictly bigger count replaces the best so far. Return -1 when no index uses any column.
        return -1;
    }

    private static Set<String> lower(Set<String> columns) {
        Set<String> lowered = new TreeSet<>();
        for (String column : columns) {
            lowered.add(column.toLowerCase(Locale.ROOT));
        }
        return lowered;
    }
}
