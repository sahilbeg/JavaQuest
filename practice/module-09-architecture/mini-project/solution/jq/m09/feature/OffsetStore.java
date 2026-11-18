package jq.m09.feature;

import java.util.HashMap;
import java.util.Map;

/**
 * Remembers, for each consumer group and partition, the offset of the next record to read.
 * It lives outside the consumer, so it survives when a consumer crashes and restarts.
 */
public class OffsetStore {

    private final Map<String, Long> committed = new HashMap<>();

    /** The next offset this group should read in this partition; 0 if it never committed. */
    public long committed(String group, int partition) {
        return committed.getOrDefault(group + "/" + partition, 0L);
    }

    /** Saves the next offset this group should read in this partition. */
    public void commit(String group, int partition, long nextOffset) {
        committed.put(group + "/" + partition, nextOffset);
    }
}
