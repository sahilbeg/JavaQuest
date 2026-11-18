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

    public long append(String entry) {
        entries.add(entry);
        return entries.size();
    }

    public long lastPosition() {
        return entries.size();
    }

    public void replicateTo(String follower, long position) {
        Long current = applied.get(follower);
        if (current == null) {
            throw new IllegalArgumentException("no follower called " + follower);
        }
        if (position > entries.size()) {
            throw new IllegalArgumentException(follower + " cannot apply position " + position
                    + ": the leader only has " + entries.size());
        }
        if (position < current) {
            throw new IllegalArgumentException(follower + " is at " + current + " and never goes back to " + position);
        }
        applied.put(follower, position);
    }

    public long appliedPosition(String replica) {
        if (replica.equals(LEADER)) {
            return entries.size();
        }
        Long position = applied.get(replica);
        if (position == null) {
            throw new IllegalArgumentException("no replica called " + replica);
        }
        return position;
    }

    public List<String> read(String replica) {
        return List.copyOf(entries.subList(0, (int) appliedPosition(replica)));
    }
}
