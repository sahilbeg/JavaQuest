package jq.m08.incident;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** One rider unlocks several bikes for a family ride. Each bike succeeds or fails on its own. */
@Service
public class GroupRentals {

    private final RentalService rentals;
    private final BikeRepository bikes;
    private final RentalRepository rentalRepository;
    private final PaymentGateway payments;
    private final BikeLock locks;

    public GroupRentals(RentalService rentals, BikeRepository bikes, RentalRepository rentalRepository,
            PaymentGateway payments, BikeLock locks) {
        this.rentals = rentals;
        this.bikes = bikes;
        this.rentalRepository = rentalRepository;
        this.payments = payments;
        this.locks = locks;
    }

    public GroupResult unlockAll(String riderId, List<String> bikeIds) {
        var unlocked = new ArrayList<String>();
        var failed = new ArrayList<String>();
        for (String bikeId : bikeIds) {
            try {
                // TODO gold: this call goes through `this`, not through Spring, so the @Transactional
                //            on unlockOne never starts a transaction. RentalService already does this work.
                unlockOne(riderId, bikeId);
                unlocked.add(bikeId);
            } catch (PaymentDeclinedException | RuntimeException e) {
                failed.add(bikeId);
            }
        }
        return new GroupResult(unlocked, failed);
    }

    /** Copied from RentalService during the rush-hour sprint, "to save time". */
    @Transactional(rollbackFor = PaymentDeclinedException.class)
    public void unlockOne(String riderId, String bikeId) throws PaymentDeclinedException {
        Bike bike = bikes.findById(bikeId).orElseThrow(() -> new BikeUnavailableException(bikeId));
        if (bike.getStatus() != BikeStatus.AVAILABLE) {
            throw new BikeUnavailableException(bikeId);
        }
        String station = bike.getStationId();
        bike.markRented();
        bikes.save(bike);
        rentalRepository.save(new Rental(riderId, bikeId, station));
        payments.hold(riderId, RentalService.UNLOCK_HOLD_CENTS);
        locks.open(bikeId);
    }
}
