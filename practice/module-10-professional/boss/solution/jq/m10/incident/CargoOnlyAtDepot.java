package jq.m10.incident;

import java.util.Optional;

/** Only Depot has docks wide enough for a cargo bike. */
public class CargoOnlyAtDepot implements ReservationRule {

    @Override
    public Optional<String> check(ReservationRequest request) {
        if (request.bikeType() == BikeType.CARGO && !request.stationId().equals("depot")) {
            return Optional.of("cargo-needs-depot");
        }
        return Optional.empty();
    }
}
