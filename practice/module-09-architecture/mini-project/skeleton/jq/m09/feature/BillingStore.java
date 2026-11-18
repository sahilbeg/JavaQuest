package jq.m09.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Billing's own database, in memory. It survives when a consumer crashes and restarts.
 * In a real database, a charge row and its "processed" mark are saved in one transaction.
 */
public class BillingStore {

    private final Set<String> processed = new HashSet<>();
    private final List<Charge> charges = new ArrayList<>();
    private final List<DeadLetter> deadLetters = new ArrayList<>();

    /** True when this event was already charged or dead-lettered. */
    public boolean isProcessed(String eventId) {
        return processed.contains(eventId);
    }

    /** Saves a successful charge and marks its event as processed. */
    public void recordCharge(RideEnded ride, String paymentId) {
        charges.add(new Charge(ride.eventId(), ride.riderId(), ride.fareCents(), paymentId));
        processed.add(ride.eventId());
    }

    /** Puts the event on the dead-letter queue and marks it as processed. */
    public void deadLetter(RideEnded ride, String reason) {
        deadLetters.add(new DeadLetter(ride, reason));
        processed.add(ride.eventId());
    }

    public List<Charge> charges() {
        return List.copyOf(charges);
    }

    public List<DeadLetter> deadLetters() {
        return List.copyOf(deadLetters);
    }

    /** The rider's charged rides: the total in cents and how many. Dead letters do not count. */
    public Statement statement(String riderId) {
        // TODO 12: add up the cents of this rider's charges and count them.
        return new Statement(riderId, 0, 0);
    }
}
