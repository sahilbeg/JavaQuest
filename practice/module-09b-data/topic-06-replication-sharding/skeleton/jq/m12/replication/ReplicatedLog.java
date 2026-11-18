package jq.m12.replication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A leader's log and how far each follower has applied it. Positions count from 1: the first entry is at position 1.
 * A replica that has applied up to position p shows exactly the first p entries.
 */
public final class ReplicatedLog {

    public static final String LEADER = "leader";

    private final List<String> entries = new ArrayList<>();
    private final Map<String, Long> applied = new LinkedHashMap<>();   // follower -> last applied position

    /** Given, finished: every follower starts at position 0 (it has applied nothing). */
    public ReplicatedLog(List<String> followers) {
        Set<String> seen = new HashSet<>();
        for (String follower : followers) {
            if (follower.equals(LEADER) || !seen.add(follower)) {
                throw new IllegalArgumentException("follower names must be unique and not \"leader\": " + follower);
            }
            applied.put(follower, 0L);
        }
    }

    /** Given, finished: the followers in the order they were given. */
    public List<String> followers() {
        return List.copyOf(applied.keySet());
    }

    /** The leader appends an entry and returns its position: 1 for the first entry, then 2, 3... */
    public long append(String entry) {
        // TODO 9
        return 0;
    }

    /** The position of the leader's newest entry, 0 when the log is empty. */
    public long lastPosition() {
        // TODO 10
        return 0;
    }

    /** The follower applies the leader's entries up to this position. */
    public void replicateTo(String follower, long position) {
        // TODO 11: IllegalArgumentException for an unknown follower (the leader included), for a position past
        //          lastPosition(), and for a position below the follower's current one. The same position is fine.
    }

    /** How far a replica has applied the log. LEADER is always at lastPosition(). Unknown name: IllegalArgumentException. */
    public long appliedPosition(String replica) {
        // TODO 12
        return 0;
    }

    /** The entries a replica can show: the first appliedPosition(replica) entries, oldest first. */
    public List<String> read(String replica) {
        // TODO 13
        return List.of();
    }
}
