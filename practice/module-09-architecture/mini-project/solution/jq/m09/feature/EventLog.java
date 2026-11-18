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
        if (partitionCount < 1) {
            throw new IllegalArgumentException("a log needs at least 1 partition, got " + partitionCount);
        }
        for (int i = 0; i < partitionCount; i++) {
            partitions.add(new ArrayList<>());
        }
    }

    /** How many partitions this log has. */
    public int partitions() {
        return partitions.size();
    }

    /** The partition every record with this key goes to. */
    public int partitionFor(String key) {
        return Math.floorMod(key.hashCode(), partitions.size());
    }

    /** Adds the event to the end of its key's partition and returns its offset there. */
    public long append(String key, RideEnded event) {
        int partition = partitionFor(key);
        List<LogRecord> records = partitions.get(partition);
        long offset = records.size();
        records.add(new LogRecord(partition, offset, key, event));
        return offset;
    }

    /** Up to max records of one partition, in order, starting at fromOffset. */
    public List<LogRecord> read(int partition, long fromOffset, int max) {
        List<LogRecord> records = recordsOf(partition);
        if (fromOffset >= records.size()) {
            return List.of();
        }
        int from = (int) fromOffset;
        int to = (int) Math.min(records.size(), fromOffset + max);
        return List.copyOf(records.subList(from, to));
    }

    /** The offset the next record in this partition will get: the number of records in it. */
    public long endOffset(int partition) {
        return recordsOf(partition).size();
    }

    private List<LogRecord> recordsOf(int partition) {
        if (partition < 0 || partition >= partitions.size()) {
            throw new IllegalArgumentException("no partition " + partition + " in a log with "
                    + partitions.size() + " partitions");
        }
        return partitions.get(partition);
    }
}
