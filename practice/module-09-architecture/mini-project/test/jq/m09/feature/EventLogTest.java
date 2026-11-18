package jq.m09.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventLogTest {

    @Test
    @DisplayName("A log needs at least one partition")
    void needsAPartition() {
        assertThrows(IllegalArgumentException.class, () -> new EventLog(0),
                "new EventLog(0) should throw IllegalArgumentException: a log with no partitions cannot hold anything");
    }

    @Test
    @DisplayName("A key always goes to the same partition: Math.floorMod of its hashCode")
    void stablePartitions() {
        EventLog log = new EventLog(4);
        assertEquals(4, log.partitions(), "the log was made with 4 partitions");
        for (String rider : List.of("rider-7", "rider-8", "rider-9", "rider-31", "rider-40")) {
            int partition = log.partitionFor(rider);
            assertEquals(Math.floorMod(rider.hashCode(), 4), partition,
                    rider + " should go to Math.floorMod(\"" + rider + "\".hashCode(), 4)");
            assertEquals(partition, log.partitionFor(rider), rider + " must get the same partition every time");
        }
        assertEquals(1, log.partitionFor("rider-31"),
                "\"rider-31\".hashCode() is negative; % would give -3, but floorMod gives 1, a real partition");
    }

    @Test
    @DisplayName("Offsets count from 0 in each partition on its own")
    void offsetsPerPartition() {
        EventLog log = new EventLog(4);
        assertEquals(0, log.append("rider-7", Rides.EVT_1001), "the first record in rider-7's partition gets offset 0");
        assertEquals(1, log.append("rider-7", Rides.EVT_1003), "the second record in that partition gets offset 1");
        assertEquals(0, log.append("rider-8", Rides.EVT_1002),
                "rider-8 is in another partition, which has its own offsets starting at 0");
        assertEquals(2, log.endOffset(log.partitionFor("rider-7")), "rider-7's partition holds 2 records");
        assertEquals(1, log.endOffset(log.partitionFor("rider-8")), "rider-8's partition holds 1 record");
    }

    @Test
    @DisplayName("read returns at most max records from fromOffset, in order, with their place in the log")
    void readFromAnOffset() {
        EventLog log = new EventLog(1);
        RideEnded[] rides = new RideEnded[5];
        for (int i = 0; i < 5; i++) {
            rides[i] = Rides.ride("evt-200" + i, "rider-7", 5 + i);
            log.append("rider-7", rides[i]);
        }
        List<LogRecord> middle = log.read(0, 1, 2);
        assertEquals(2, middle.size(), "read(0, 1, 2) should return 2 records");
        assertEquals(new LogRecord(0, 1, "rider-7", rides[1]), middle.get(0),
                "the first record read should be offset 1, with its partition, key and event");
        assertEquals(new LogRecord(0, 2, "rider-7", rides[2]), middle.get(1), "then offset 2");
        List<LogRecord> rest = log.read(0, 3, 10);
        assertEquals(2, rest.size(), "from offset 3 only 2 records are left, even though max is 10");
        assertEquals(4, rest.get(1).offset(), "the last record has offset 4");
        assertTrue(log.read(0, 5, 10).isEmpty(), "reading from the end offset gives an empty list");
    }

    @Test
    @DisplayName("Asking for a partition that does not exist is an error")
    void unknownPartition() {
        EventLog log = new EventLog(4);
        assertThrows(IllegalArgumentException.class, () -> log.read(4, 0, 10),
                "a log with 4 partitions has partitions 0 to 3; read(4, ...) should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> log.read(-1, 0, 10),
                "read(-1, ...) should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> log.endOffset(4),
                "endOffset(4) should throw IllegalArgumentException");
    }
}
