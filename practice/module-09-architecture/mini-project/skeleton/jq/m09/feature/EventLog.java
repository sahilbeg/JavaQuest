package jq.m09.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * An in-memory, append-only log split into partitions, like one topic in Kafka.
 * Records are never changed or removed. Each partition keeps its own order.
 */
public class EventLog {

    private final List<List<LogRecord>> partitions = new ArrayList<>();

    public EventLog(int partitionCount) {
        // TODO 2: throw IllegalArgumentException when partitionCount is below 1.
        // Then add one empty ArrayList to partitions for each partition.
    }

    /** How many partitions this log has. */
    public int partitions() {
        return partitions.size();
    }

    /** The partition every record with this key goes to. */
    public int partitionFor(String key) {
        // TODO 3: Math.floorMod of the key's hashCode and the number of partitions.
        return 0;
    }

    /** Adds the event to the end of its key's partition and returns its offset there. */
    public long append(String key, RideEnded event) {
        // TODO 4: find the key's partition. The new record's offset is the number of records
        // already in it. Add a new LogRecord(partition, offset, key, event), then return the offset.
        return -1;
    }

    /** Up to max records of one partition, in order, starting at fromOffset. */
    public List<LogRecord> read(int partition, long fromOffset, int max) {
        // TODO 5: throw IllegalArgumentException for a partition that does not exist.
        // Return the records from fromOffset, at most max of them, as a new list.
        // Return an empty list when fromOffset is at or past the end.
        return List.of();
    }

    /** The offset the next record in this partition will get: the number of records in it. */
    public long endOffset(int partition) {
        // TODO 6: throw IllegalArgumentException for a partition that does not exist.
        // Otherwise return how many records the partition holds.
        return 0;
    }
}
