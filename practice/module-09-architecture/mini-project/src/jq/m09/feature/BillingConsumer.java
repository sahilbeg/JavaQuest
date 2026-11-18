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
        // TODO 8: for each partition, from 0 upwards: read up to maxPerPartition records from
        // the group's committed offset. For each record: handle its event FIRST, then commit
        // record.offset() + 1. Count the records and return the count.
        return 0;
    }

    /** Charges one ride, at most once, or puts it on the dead-letter queue. */
    public void handle(RideEnded ride) {
        // TODO 9: if the store already processed this eventId, do nothing: it is a redelivery.
        // TODO 10: compare ride.fareCents() with Fares.expectedCents(type, minutes). If they differ,
        // dead-letter the ride with a reason that starts with "fare mismatch", and charge nothing.
        // TODO 11: charge ride.fareCents() with the eventId as the idempotency key, and record the charge.
        // A PaymentFailedException means try again, up to MAX_ATTEMPTS attempts in all. If every
        // attempt fails, dead-letter the ride with a reason that starts with "payment failed".
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
