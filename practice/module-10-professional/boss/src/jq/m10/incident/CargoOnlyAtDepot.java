package jq.m10.incident;

import java.util.Optional;

/** Only Depot has docks wide enough for a cargo bike. */
public class CargoOnlyAtDepot implements ReservationRule {

    @Override
    public Optional<String> check(ReservationRequest request) {
        // TODO 3 (gold): refuse a CARGO bike at any station other than "depot", with the reason "cargo-needs-depot".
        //                Allow everything else with Optional.empty().
        return Optional.empty();
    }
}
