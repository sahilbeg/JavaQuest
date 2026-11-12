package jq.m06.debugging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CrashReportsTest {

    @Test
    @DisplayName("Crash 1: a ride line with an empty note parses without crashing")
    void parsesEmptyNote() {
        RideParser parser = new RideParser();
        ParsedRide ride = assertDoesNotThrow(() -> parser.parse("B-1042,Harbor,12,"),
                "parse(\"B-1042,Harbor,12,\") crashed. Read crash report 1: how many parts does split give you for this line?");
        assertEquals(new ParsedRide("B-1042", "Harbor", 12, ""), ride,
                "the note after the last comma is empty, so note should be \"\"");
    }

    @Test
    @DisplayName("Crash 1: a full ride line still parses every field")
    void parsesFullLine() {
        ParsedRide ride = new RideParser().parse("B-2201,Old Mill,30,flat tyre");
        assertEquals(new ParsedRide("B-2201", "Old Mill", 30, "flat tyre"), ride,
                "a line with a note should keep all four fields");
    }

    @Test
    @DisplayName("Crash 1: a line with too few fields is rejected with IllegalArgumentException")
    void rejectsShortLine() {
        RideParser parser = new RideParser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("B-1042,Harbor"),
                "parse(\"B-1042,Harbor\") has only 2 fields. It should throw IllegalArgumentException with a clear message, instead of crashing somewhere deeper");
    }

    @Test
    @DisplayName("Crash 2: a station missing from the index has 0 free docks")
    void unknownStationIsZero() {
        StationIndex index = new StationIndex();
        int docks = assertDoesNotThrow(() -> index.docksAt("Depot"),
                "docksAt(\"Depot\") crashed. Read crash report 2: what does the map give back for a key it does not have?");
        assertEquals(0, docks, "Depot is not in the index yet, so docksAt should return 0");
        assertEquals(3, index.docksAt("Harbor"), "Harbor should still report its 3 free docks");
    }

    @Test
    @DisplayName("Crash 3: losing half the ride refunds half the money")
    void halfRefund() {
        assertEquals(140, new RefundCalculator().refundCents(280, 6, 12),
                "280 cents paid and 6 of 12 minutes lost: the refund should be 140 cents. Check what 6 / 12 is in int maths");
    }

    @Test
    @DisplayName("Crash 3: an uneven refund rounds down to whole cents")
    void unevenRefundRoundsDown() {
        RefundCalculator calc = new RefundCalculator();
        assertEquals(116, calc.refundCents(280, 5, 12),
                "280 cents for 5 of 12 minutes is 116.67, so the refund should round down to 116 cents");
        assertEquals(280, calc.refundCents(280, 12, 12), "losing the whole ride refunds everything: 280 cents");
    }
}
