package jq.m12.replication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuorumReaderTest {

    private final QuorumReader reader = new QuorumReader(3, 2);

    @Test
    @DisplayName("W + R > N means every read quorum overlaps every write quorum")
    void overlapRule() {
        assertTrue(QuorumReader.overlaps(3, 2, 2), "N=3, W=2, R=2: 2 + 2 = 4 > 3, so they overlap");
        assertFalse(QuorumReader.overlaps(3, 1, 1), "N=3, W=1, R=1: 1 + 1 = 2 is not > 3, so a read can miss the write");
        assertFalse(QuorumReader.overlaps(4, 2, 2), "N=4, W=2, R=2: 4 is not > 4, so the two quorums can miss each other");
        assertTrue(QuorumReader.overlaps(3, 3, 1), "N=3, W=3, R=1: 3 + 1 = 4 > 3");
        assertThrows(IllegalArgumentException.class, () -> QuorumReader.overlaps(3, 4, 1),
                "W cannot be bigger than N: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new QuorumReader(3, 0),
                "R must be at least 1: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new QuorumReader(3, 4),
                "R cannot be bigger than N: throw IllegalArgumentException");
    }

    @Test
    @DisplayName("The newest version wins, and older replicas are listed for read repair in answer order")
    void newestWins() {
        ReadResult result = reader.read(List.of(
                new ReplicaAnswer("node-b", "rental-17: ACTIVE", 4),
                new ReplicaAnswer("node-a", "rental-17: ENDED", 5),
                new ReplicaAnswer("node-c", "rental-17: STARTING", 3)));
        assertEquals("rental-17: ENDED", result.value(), "version 5 is the newest, so its value wins");
        assertEquals(5, result.version(), "the result should carry the newest version, 5");
        assertEquals(List.of("node-b", "node-c"), result.needRepair(),
                "node-b (4) and node-c (3) are behind, in the order they answered");
    }

    @Test
    @DisplayName("When every replica agrees, nothing needs repair")
    void allAgree() {
        ReadResult result = reader.read(List.of(
                new ReplicaAnswer("node-a", "rental-17: ENDED", 5),
                new ReplicaAnswer("node-b", "rental-17: ENDED", 5)));
        assertEquals("rental-17: ENDED", result.value(), "both replicas hold version 5");
        assertEquals(List.of(), result.needRepair(), "no replica is behind, so the repair list is empty");
    }

    @Test
    @DisplayName("Fewer answers than R fails clearly with QuorumNotReachedException")
    void quorumNotReached() {
        QuorumNotReachedException error = assertThrows(QuorumNotReachedException.class,
                () -> reader.read(List.of(new ReplicaAnswer("node-a", "rental-17: ENDED", 5))),
                "R is 2 but only 1 replica answered: throw QuorumNotReachedException, never guess");
        assertEquals("needed 2 answers, got 1", error.getMessage(), "the message should say how many were needed and how many came");
        assertThrows(QuorumNotReachedException.class, () -> reader.read(List.of()),
                "no answers at all is also below the quorum");
    }

    @Test
    @DisplayName("A replica with no copy counts as version 0 and needs repair; if nobody has a copy, the value is null")
    void missingCopies() {
        ReadResult result = reader.read(List.of(
                new ReplicaAnswer("node-a", null, 0),
                new ReplicaAnswer("node-c", "rental-17: ACTIVE", 1)));
        assertEquals("rental-17: ACTIVE", result.value(), "node-c's version 1 beats node-a's missing copy");
        assertEquals(List.of("node-a"), result.needRepair(), "node-a has no copy, so it needs repair");

        ReadResult nothing = reader.read(List.of(new ReplicaAnswer("node-a", null, 0), new ReplicaAnswer("node-b", null, 0)));
        assertNull(nothing.value(), "no replica holds a copy, so the value should be null");
        assertEquals(List.of(), nothing.needRepair(), "everyone agrees there is nothing, so nothing needs repair");
    }

    @Test
    @DisplayName("Two different values with the same newest version are a conflict, not a guess")
    void conflict() {
        assertThrows(IllegalStateException.class, () -> reader.read(List.of(
                        new ReplicaAnswer("node-a", "rental-17: ENDED at harbor", 6),
                        new ReplicaAnswer("node-b", "rental-17: ENDED at uptown", 6))),
                "both say version 6 but disagree: throw IllegalStateException, because a version cannot tell them apart");
    }
}
