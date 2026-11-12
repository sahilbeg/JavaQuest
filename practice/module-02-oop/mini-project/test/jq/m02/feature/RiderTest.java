package jq.m02.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiderTest {

    @Test
    @DisplayName("the enums carry the prices and membership rules")
    void enums() {
        assertEquals(0.30, BikeType.EBIKE.pricePerMinute(), 0.001, "EBIKE costs 0.30 per minute");
        assertEquals(0.25, BikeType.CARGO.pricePerMinute(), 0.001, "CARGO costs 0.25 per minute");
        assertEquals(30, Membership.PLUS.freeMinutes(), "PLUS riders get 30 free minutes");
        assertEquals(60, Membership.PRO.freeMinutes(), "PRO riders get 60 free minutes");
        assertTrue(Membership.BASIC.paysUnlockFee(), "BASIC riders pay the unlock fee");
        assertFalse(Membership.PRO.paysUnlockFee(), "PRO riders do not pay the unlock fee");
    }

    @Test
    @DisplayName("each membership level prices a ride differently")
    void fares() {
        Ride ride = new Ride(BikeType.EBIKE, 40);
        assertEquals(13.00, new Rider("Bo", Membership.BASIC).fareFor(ride), 0.001, "BASIC: 1.00 + 40 x 0.30 = 13.00");
        assertEquals(3.00, new Rider("Bo", Membership.PLUS).fareFor(ride), 0.001, "PLUS: 10 paid minutes x 0.30 = 3.00");
        assertEquals(0.00, new Rider("Bo", Membership.PRO).fareFor(ride), 0.001, "PRO: 40 minutes are all free");
    }

    @Test
    @DisplayName("rides are stored up to 50 and added up")
    void storesRides() {
        Rider rider = new Rider("Cy", Membership.BASIC);
        for (int i = 0; i < 55; i++) {
            rider.recordRide(new Ride(BikeType.CLASSIC, 10));
        }
        assertEquals(50, rider.rideCount(), "only 50 rides fit; extra rides are ignored");
        assertEquals(125.00, rider.monthTotal(), 0.001, "50 rides x (1.00 + 10 x 0.15) = 125.00");
    }

    @Test
    @DisplayName("an upgrade changes the price of rides")
    void upgrade() {
        Rider rider = new Rider("Di", Membership.BASIC);
        rider.recordRide(new Ride(BikeType.CARGO, 50));
        assertEquals(13.50, rider.monthTotal(), 0.001, "as BASIC, 50 cargo minutes cost 1.00 + 50 x 0.25 = 13.50");
        rider.upgrade(Membership.PRO);
        assertEquals(0.00, rider.monthTotal(), 0.001, "after upgrading to PRO, a 50 minute ride is free");
    }

    @Test
    @DisplayName("the statement matches the layout in the KB page")
    void statement() {
        Rider ada = new Rider("Ada", Membership.PLUS);
        ada.recordRide(new Ride(BikeType.EBIKE, 40));
        ada.recordRide(new Ride(BikeType.CLASSIC, 20));
        ada.recordRide(new Ride(BikeType.CARGO, 90));
        String expected = """
                Statement for Ada (PLUS)
                EBIKE      40 min   $3.00
                CLASSIC    20 min   $0.00
                CARGO      90 min  $15.00
                Total              $18.00""";
        assertEquals(expected, ada.statement(), "compare with the statement in the KB page, line by line");
    }
}
