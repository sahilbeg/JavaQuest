package jq.m09.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OffsetsTest {

    /** An offset store whose first commit fails, as if the server died just before it. */
    static final class CrashBeforeFirstCommit extends OffsetStore {
        private boolean crashed;

        @Override
        public void commit(String group, int partition, long nextOffset) {
            if (!crashed) {
                crashed = true;
                throw new IllegalStateException("billing server crashed before the commit");
            }
            super.commit(group, partition, nextOffset);
        }
    }

    @Test
    @DisplayName("Each consumer group keeps its own offset for each partition, starting at 0")
    void offsetsPerGroup() {
        OffsetStore offsets = new OffsetStore();
        assertEquals(0, offsets.committed("billing", 0), "a group that never committed starts at offset 0");
        offsets.commit("billing", 0, 3);
        assertEquals(3, offsets.committed("billing", 0), "billing committed offset 3 in partition 0");
        assertEquals(0, offsets.committed("billing", 1), "partition 1 has its own offset, still 0");
        assertEquals(0, offsets.committed("analytics", 0), "the analytics group has its own offsets, still 0");
        offsets.commit("billing", 0, 5);
        assertEquals(5, offsets.committed("billing", 0), "a new commit replaces the old offset");
    }

    @Test
    @DisplayName("The offset is committed only after the event is handled")
    void commitAfterHandling() {
        EventLog log = new EventLog(1);
        OffsetStore offsets = new OffsetStore();
        FakeGateway gateway = new FakeGateway();
        List<Long> committedWhileCharging = new ArrayList<>();
        gateway.beforeEachCharge(() -> committedWhileCharging.add(offsets.committed(BillingConsumer.GROUP, 0)));
        Rides.publish(log, Rides.EVT_1001, Rides.EVT_1002, Rides.EVT_1003);

        BillingConsumer consumer = new BillingConsumer(log, offsets, new BillingStore(), gateway);
        assertEquals(3, consumer.poll(10), "poll should handle all 3 records");
        assertEquals(List.of(0L, 1L, 2L), committedWhileCharging,
                "while record N is being charged, the committed offset must still be N; commit only after handling it");
        assertEquals(3, offsets.committed(BillingConsumer.GROUP, 0),
                "after handling offsets 0, 1 and 2, the committed offset is 3: the next record to read");
    }

    @Test
    @DisplayName("poll reads at most maxPerPartition records, and lag shows how many are left")
    void pollAndLag() {
        EventLog log = new EventLog(1);
        for (int i = 0; i < 5; i++) {
            Rides.publish(log, Rides.ride("evt-300" + i, "rider-7", 3));
        }
        BillingConsumer consumer = new BillingConsumer(log, new OffsetStore(), new BillingStore(), new FakeGateway());
        assertEquals(5, consumer.lag(), "5 records are waiting before the first poll");
        assertEquals(2, consumer.poll(2), "poll(2) handles at most 2 records from the partition");
        assertEquals(3, consumer.lag(), "3 records are still waiting");
        assertEquals(3, consumer.poll(10), "the next poll picks up where the commit left off");
        assertEquals(0, consumer.lag(), "billing has caught up");
        assertEquals(0, consumer.poll(10), "nothing is left to read");
    }

    @Test
    @DisplayName("After a crash before the commit, the event comes again, but the rider is charged only once")
    void crashAndRedeliver() {
        EventLog log = new EventLog(1);
        OffsetStore offsets = new CrashBeforeFirstCommit();
        BillingStore store = new BillingStore();
        FakeGateway gateway = new FakeGateway();
        Rides.publish(log, Rides.EVT_1001);

        BillingConsumer first = new BillingConsumer(log, offsets, store, gateway);
        assertThrows(IllegalStateException.class, () -> first.poll(10),
                "the commit fails here, as if the server died: poll should let that exception out");
        assertEquals(1, gateway.attempts().size(), "the first consumer charged evt-1001 before it crashed");

        BillingConsumer restarted = new BillingConsumer(log, offsets, store, gateway);
        assertEquals(1, restarted.poll(10),
                "the offset was never committed, so the restarted consumer reads evt-1001 again");
        assertEquals(1, gateway.attempts().size(),
                "evt-1001 was already charged; a redelivered event must not charge the rider again");
        assertEquals(1, offsets.committed(BillingConsumer.GROUP, 0), "this time the offset is committed");
        assertEquals(new Statement("rider-7", 460, 1), store.statement("rider-7"), "rider-7 paid for one ride, once");
    }
}
