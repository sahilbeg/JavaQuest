package jq.m09.feature;

import java.util.List;

/**
 * Billing's consumer: reads RideEnded events from the log and charges riders.
 * It runs on one thread. Delivery is at least once, so handling must be idempotent.
 */
public class BillingConsumer {

    /** The consumer group billing commits its offsets under. */
    public static final String GROUP = "billing";

    /** How many times one charge is tried before it goes to the dead-letter queue. */
    public static final int MAX_ATTEMPTS = 3;

    private final EventLog log;
    private final OffsetStore offsets;
    private final BillingStore store;
    private final PaymentGateway gateway;

    public BillingConsumer(EventLog log, OffsetStore offsets, BillingStore store, PaymentGateway gateway) {
        this.log = log;
        this.offsets = offsets;
        this.store = store;
        this.gateway = gateway;
    }

    /**
     * Reads up to maxPerPartition records from each partition, from partition 0 upwards,
     * starting at the group's committed offset. Handles each record, then commits.
     *
     * @return how many records were handled in this poll
     */
    public int poll(int maxPerPartition) {
        int handled = 0;
        for (int partition = 0; partition < log.partitions(); partition++) {
            long from = offsets.committed(GROUP, partition);
            List<LogRecord> records = log.read(partition, from, maxPerPartition);
            for (LogRecord record : records) {
                handle(record.event());
                offsets.commit(GROUP, partition, record.offset() + 1);
                handled++;
            }
        }
        return handled;
    }

    /** Charges one ride, at most once, or puts it on the dead-letter queue. */
    public void handle(RideEnded ride) {
        if (store.isProcessed(ride.eventId())) {
            return;
        }
        long expected = Fares.expectedCents(ride.type(), ride.minutes());
        if (ride.fareCents() != expected) {
            store.deadLetter(ride, "fare mismatch: expected " + expected + " cents, event says " + ride.fareCents());
            return;
        }
        PaymentFailedException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String paymentId = gateway.charge(ride.riderId(), ride.fareCents(), ride.eventId());
                store.recordCharge(ride, paymentId);
                return;
            } catch (PaymentFailedException e) {
                lastFailure = e;
            }
        }
        store.deadLetter(ride, "payment failed after " + MAX_ATTEMPTS + " attempts: " + lastFailure.getMessage());
    }

    /** How many records are in the log that this group has not committed yet, over every partition. */
    public long lag() {
        long lag = 0;
        for (int partition = 0; partition < log.partitions(); partition++) {
            lag += log.endOffset(partition) - offsets.committed(GROUP, partition);
        }
        return lag;
    }
}
