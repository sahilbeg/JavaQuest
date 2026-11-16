package jq.m08.incident;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** One rider unlocks several bikes for a family ride. Each bike succeeds or fails on its own. */
@Service
public class GroupRentals {

    private final RentalService rentals;

    public GroupRentals(RentalService rentals) {
        this.rentals = rentals;
    }

    public GroupResult unlockAll(String riderId, List<String> bikeIds) {
        var unlocked = new ArrayList<String>();
        var failed = new ArrayList<String>();
        for (String bikeId : bikeIds) {
            try {
                rentals.startRental(riderId, bikeId);
                unlocked.add(bikeId);
            } catch (PaymentDeclinedException | RuntimeException e) {
                failed.add(bikeId);
            }
        }
        return new GroupResult(unlocked, failed);
    }
}
