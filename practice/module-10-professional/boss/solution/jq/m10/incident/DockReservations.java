package jq.m10.incident;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Round 3, the design round: riders hold a dock before they return a bike.
 * Business rules plug in from outside, and two riders can never hold the same dock.
 */
public class DockReservations {

    private final List<ReservationRule> rules;
    private final Map<String, String> holds = new ConcurrentHashMap<>();   // dock id -> rider id

    public DockReservations(List<ReservationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public ReservationResult reserve(ReservationRequest request) {
        if (holds.containsKey(request.dockId())) {
            return ReservationResult.refused("dock-taken");     // cheap early answer, no rules needed
        }
        for (ReservationRule rule : rules) {
            Optional<String> reason = rule.check(request);
            if (reason.isPresent()) {
                return ReservationResult.refused(reason.get());
            }
        }
        // Check and claim in one atomic step: only one rider can win the dock.
        String earlier = holds.putIfAbsent(request.dockId(), request.riderId());
        return earlier == null ? ReservationResult.ok() : ReservationResult.refused("dock-taken");
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
