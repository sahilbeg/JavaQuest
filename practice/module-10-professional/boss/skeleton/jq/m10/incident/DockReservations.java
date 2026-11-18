package jq.m10.incident;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Round 3, the design round: riders hold a dock before they return a bike.
 * Business rules plug in from outside, and two riders can never hold the same dock.
 *
 * This is the candidate's design. The interviewer asked for rules that plug in;
 * the candidate added the constructor parameter and never used it.
 */
public class DockReservations {

    private final List<ReservationRule> rules;
    private final Map<String, String> holds = new HashMap<>();   // dock id -> rider id

    public DockReservations(List<ReservationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public ReservationResult reserve(ReservationRequest request) {
        if (holds.containsKey(request.dockId())) {
            return ReservationResult.refused("dock-taken");
        }
        // TODO 4 (gold): a rule welded into reserve. A new rule means editing this class again,
        //                and the rules handed to the constructor never run.
        if (request.bikeType() == BikeType.CARGO && !request.stationId().equals("depot")) {
            return ReservationResult.refused("cargo-needs-depot");
        }
        // TODO 5 (gold): two riders tapping at the same moment can both pass the check above,
        //                and then both put. The check and the claim must be one atomic step.
        holds.put(request.dockId(), request.riderId());
        return ReservationResult.ok();
    }

    /** The rider holding this dock, if any. */
    public Optional<String> holder(String dockId) {
        return Optional.ofNullable(holds.get(dockId));
    }

    /** Lets go of a hold. Only the rider who holds the dock can release it. */
    public boolean release(String dockId, String riderId) {
        return holds.remove(dockId, riderId);
    }

    public int heldCount() {
        return holds.size();
    }
}
