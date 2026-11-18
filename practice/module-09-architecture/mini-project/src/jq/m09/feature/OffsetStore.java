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
        // TODO 7: look up the offset saved for this group and partition, or 0.
        // One map key per pair works, for example group + "/" + partition.
        return 0;
    }

    /** Saves the next offset this group should read in this partition. */
    public void commit(String group, int partition, long nextOffset) {
        // TODO 7: save nextOffset for this group and partition.
    }
}
