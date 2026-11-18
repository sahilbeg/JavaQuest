package jq.m12.replication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Sends each session's reads to a replica that has caught up with everything the session wrote or saw. */
public final class SessionRouter {

    private final ReplicatedLog log;
    private final Map<String, Long> seen = new HashMap<>();   // session -> highest position it wrote or read

    public SessionRouter(ReplicatedLog log) {
        this.log = log;
    }

    /** Appends through the leader and remembers the position for this session. Returns the position. */
    public long write(String session, String entry) {
        // TODO 14: seen becomes the larger of what it was and the new position (Map.merge with Math::max)
        return 0;
    }

    /** The first follower, in log.followers() order, whose applied position is at least what the session has seen; else LEADER. */
    public String replicaFor(String session) {
        // TODO 15: a session that never wrote or read has seen position 0
        return null;
    }

    /** Reads from replicaFor(session), and raises the session's seen position to that replica's applied position. */
    public List<String> read(String session) {
        // TODO 16
        return List.of();
    }
}
