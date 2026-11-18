package jq.m10.capstone;

import java.util.List;
import java.util.Optional;

/** Core, driven port: where the bikes are. The fleet team's system implements it in production. */
public interface BikeInventory {

    Optional<Bike> findById(String bikeId);

    /** The bike leaves its dock: status RENTED, no station. */
    void markRented(String bikeId);

    /** The bike is docked at a station again: status AVAILABLE. */
    void dock(String bikeId, String stationId);

    /** Every bike docked at this station right now. */
    List<Bike> dockedAt(String stationId);
}
