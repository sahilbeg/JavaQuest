package jq.m01.variables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideRecordTest {

    @Test
    @DisplayName("bikeId() remembers the bike B-1042")
    void bikeIdIsStored() {
        assertEquals("B-1042", RideRecord.bikeId(),
                "bikeId() should return \"B-1042\". Check the capital B and the dash");
    }

    @Test
    @DisplayName("startStation() remembers the Riverside station")
    void startStationIsStored() {
        assertEquals("Riverside", RideRecord.startStation(),
                "startStation() should return \"Riverside\" with a capital R");
    }

    @Test
    @DisplayName("rideMinutes() remembers a ride of 20 minutes")
    void rideMinutesIsStored() {
        assertEquals(20, RideRecord.rideMinutes(),
                "rideMinutes() should return 20, the number you stored in an int variable");
    }

    @Test
    @DisplayName("minutesAfterExtension() gives the new value after the ride is extended")
    void minutesAfterExtensionIsReassigned() {
        assertEquals(35, RideRecord.minutesAfterExtension(),
                "minutesAfterExtension() should return 35: start the variable at 20, then reassign it to 35");
    }
}
