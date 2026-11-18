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
        if (indexColumns == null || indexColumns.isEmpty()) {
            throw new IllegalArgumentException("an index needs at least one column");
        }
        Objects.requireNonNull(equalityColumns, "equalityColumns");
        Set<String> equality = lower(equalityColumns);
        String range = rangeColumn == null ? null : rangeColumn.toLowerCase(Locale.ROOT);
        if (range != null && equality.contains(range)) {
            throw new IllegalArgumentException(rangeColumn + " cannot be both an equality column and the range column");
        }
        int used = 0;
        for (String column : indexColumns) {
            String name = column.toLowerCase(Locale.ROOT);
            if (equality.contains(name)) {
                used++;
            } else {
                if (name.equals(range)) {
                    used++;
                }
                break;
            }
        }
        return new IndexUse(used > 0, used);
    }

    /** The index to build for a query: its equality columns in alphabetical order, then the range column. */
    public static List<String> suggest(Set<String> equalityColumns, String rangeColumn) {
        Objects.requireNonNull(equalityColumns, "equalityColumns");
        List<String> columns = new ArrayList<>(lower(equalityColumns));
        if (rangeColumn != null) {
            String range = rangeColumn.toLowerCase(Locale.ROOT);
            if (columns.contains(range)) {
                throw new IllegalArgumentException(rangeColumn + " cannot be both an equality column and the range column");
            }
            columns.add(range);
        }
        return columns;
    }

    /** The position of the index that uses the most columns. The earliest one wins a tie. -1 when none is usable. */
    public static int bestIndex(List<List<String>> indexes, Set<String> equalityColumns, String rangeColumn) {
        int best = -1;
        int bestUsed = 0;
        for (int i = 0; i < indexes.size(); i++) {
            int used = evaluate(indexes.get(i), equalityColumns, rangeColumn).columnsUsed();
            if (used > bestUsed) {
                best = i;
                bestUsed = used;
            }
        }
        return best;
    }

    private static Set<String> lower(Set<String> columns) {
        Set<String> lowered = new TreeSet<>();
        for (String column : columns) {
            lowered.add(column.toLowerCase(Locale.ROOT));
        }
        return lowered;
    }
}
