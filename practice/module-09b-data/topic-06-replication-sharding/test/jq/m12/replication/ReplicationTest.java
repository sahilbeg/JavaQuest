package jq.m12.replication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReplicationTest {

    private final ReplicatedLog log = new ReplicatedLog(List.of("replica-1", "replica-2"));

    @Test
    @DisplayName("The leader numbers its entries 1, 2, 3, and a follower shows only the entries it has applied")
    void leaderAndFollowers() {
        assertEquals(1, log.append("rental-16 started"), "the first entry should be at position 1");
        assertEquals(2, log.append("rental-16 ended"), "the second entry should be at position 2");
        assertEquals(3, log.append("rental-17 started"), "the third entry should be at position 3");
        assertEquals(3, log.lastPosition(), "lastPosition should be 3 after three appends");

        assertEquals(List.of("rental-16 started", "rental-16 ended", "rental-17 started"), log.read(ReplicatedLog.LEADER),
                "the leader always shows every entry");
        assertEquals(List.of(), log.read("replica-1"), "a new follower has applied nothing, so it shows nothing");

        log.replicateTo("replica-1", 2);
        assertEquals(2, log.appliedPosition("replica-1"), "replica-1 should now be at position 2");
        assertEquals(List.of("rental-16 started", "rental-16 ended"), log.read("replica-1"),
                "replica-1 has applied up to 2, so rental-17 is not there yet: that is replication lag");
        assertEquals(3, log.appliedPosition(ReplicatedLog.LEADER), "the leader's applied position is its last position");
    }

    @Test
    @DisplayName("A follower never goes backwards or past the leader, and unknown names are refused")
    void replicateRules() {
        log.append("a");
        log.append("b");
        log.replicateTo("replica-2", 2);
        assertThrows(IllegalArgumentException.class, () -> log.replicateTo("replica-2", 1),
                "replica-2 is at 2; going back to 1 should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> log.replicateTo("replica-1", 3),
                "the leader only has 2 entries, so position 3 should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> log.replicateTo("replica-9", 1),
                "there is no replica-9: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> log.read("replica-9"),
                "reading from a replica that does not exist should throw IllegalArgumentException");
        log.replicateTo("replica-2", 2);
        assertEquals(2, log.appliedPosition("replica-2"), "replicating to the same position again changes nothing");
    }

    @Test
    @DisplayName("Read-your-writes: right after a write, the session reads from the leader while followers lag")
    void readYourWritesGoesToLeader() {
        SessionRouter router = new SessionRouter(log);
        long position = router.write("ada", "rental-17 started");
        assertEquals(1, position, "the write should return the position the leader gave it");

        assertEquals(ReplicatedLog.LEADER, router.replicaFor("ada"),
                "no follower has applied position 1 yet, so Ada's read must go to the leader");
        assertEquals(List.of("rental-17 started"), router.read("ada"), "Ada must see the rental she just started");
    }

    @Test
    @DisplayName("Once a follower catches up, the session reads from the first follower that has its write")
    void caughtUpFollowerIsUsed() {
        SessionRouter router = new SessionRouter(log);
        router.write("ada", "rental-17 started");
        router.write("ada", "rental-17 ended");
        log.replicateTo("replica-1", 1);
        log.replicateTo("replica-2", 2);

        assertEquals("replica-2", router.replicaFor("ada"),
                "replica-1 is at 1 but Ada wrote position 2; replica-2 has reached 2, so use replica-2");
        log.replicateTo("replica-1", 2);
        assertEquals("replica-1", router.replicaFor("ada"),
                "both followers have position 2 now; pick the first one in the list, replica-1");
    }

    @Test
    @DisplayName("A session that never wrote reads from the first follower, even while another rider's write lags")
    void otherSessionsAreNotForcedToLeader() {
        SessionRouter router = new SessionRouter(log);
        router.write("ada", "rental-17 started");
        assertEquals("replica-1", router.replicaFor("ben"),
                "Ben wrote nothing and read nothing, so any follower will do: the first, replica-1");
        assertEquals(List.of(), router.read("ben"), "Ben may not see Ada's rental yet; that is fine for him");
    }

    @Test
    @DisplayName("Monotonic reads: after seeing position 2, a session is never sent to a follower that is behind it")
    void monotonicReads() {
        SessionRouter router = new SessionRouter(log);
        router.write("dee", "rental-17 started");      // position 1
        router.write("ben", "rental-18 started");      // position 2, another rider

        assertEquals(List.of("rental-17 started", "rental-18 started"), router.read("dee"),
                "no follower has Dee's write yet, so she reads from the leader and sees both entries");

        log.replicateTo("replica-1", 1);
        assertEquals(ReplicatedLog.LEADER, router.replicaFor("dee"),
                "replica-1 has Dee's write (1) but not position 2, which Dee has already SEEN; it would make rental-18 vanish");
        log.replicateTo("replica-2", 2);
        assertEquals("replica-2", router.replicaFor("dee"), "replica-2 has reached 2, so it may answer Dee");
        assertEquals(List.of("rental-17 started", "rental-18 started"), router.read("dee"),
                "from replica-2 Dee still sees both entries: time never goes backwards for her");
    }
}
