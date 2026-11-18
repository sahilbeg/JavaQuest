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

    public long write(String session, String entry) {
        long position = log.append(entry);
        seen.merge(session, position, Math::max);
        return position;
    }

    public String replicaFor(String session) {
        long needed = seen.getOrDefault(session, 0L);
        for (String follower : log.followers()) {
            if (log.appliedPosition(follower) >= needed) {
                return follower;
            }
        }
        return ReplicatedLog.LEADER;
    }

    public List<String> read(String session) {
        String replica = replicaFor(session);
        seen.merge(session, log.appliedPosition(replica), Math::max);
        return log.read(replica);
    }
}
