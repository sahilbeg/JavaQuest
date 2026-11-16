package jq.m08.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** Shared set-up for the three tiers: five docked bikes, a bank that declines r-broke, and a lock that jams on B-2002. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
abstract class IncidentTestBase {

    @Autowired
    RentalService rentalService;

    @Autowired
    GroupRentals groupRentals;

    @Autowired
    BikeRepository bikes;

    @Autowired
    RentalRepository rentals;

    @MockitoBean
    PaymentGateway payments;

    @MockitoBean
    BikeLock locks;

    @BeforeEach
    void dockFiveBikes() throws Exception {
        rentals.deleteAll();
        bikes.deleteAll();
        bikes.save(new Bike("B-1042", BikeStatus.AVAILABLE, "harbor"));
        bikes.save(new Bike("B-2001", BikeStatus.AVAILABLE, "harbor"));
        bikes.save(new Bike("B-2002", BikeStatus.AVAILABLE, "harbor"));
        bikes.save(new Bike("B-2003", BikeStatus.AVAILABLE, "harbor"));
        bikes.save(new Bike("B-3001", BikeStatus.AVAILABLE, "uptown"));
        doThrow(new PaymentDeclinedException("r-broke")).when(payments).hold(eq("r-broke"), anyLong());
        doThrow(new LockJammedException("B-2002")).when(locks).open("B-2002");
    }

    /** Reads the bike back from the database, the way the next request would see it. */
    void assertStillDocked(String bikeId, String station, String why) {
        Bike bike = bikes.findById(bikeId).orElseThrow();
        assertEquals(BikeStatus.AVAILABLE, bike.getStatus(), bikeId + " should be AVAILABLE again: " + why);
        assertEquals(station, bike.getStationId(), bikeId + " should still be docked at " + station + ": " + why);
        assertTrue(rentals.findByBikeId(bikeId).isEmpty(), bikeId + " should have no rental: " + why);
    }

    void assertRented(String bikeId, String riderId) {
        assertEquals(BikeStatus.RENTED, bikes.findById(bikeId).orElseThrow().getStatus(), bikeId + " should be RENTED");
        var found = rentals.findByBikeId(bikeId);
        assertEquals(1, found.size(), bikeId + " should have exactly one rental");
        assertEquals(riderId, found.getFirst().getRiderId(), bikeId + "'s rental should belong to " + riderId);
    }
}
