package jq.m03.optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeLookupTest {

    private final BikeLookup lookup = new BikeLookup(Map.of("B-1042", "Riverside", "B-2001", "Harbor"));

    private static final List<Ride> RIDES = List.of(
            new Ride("B-2001", "Harbor", "classic", 10),
            new Ride("B-1042", "Riverside", "ebike", 80),
            new Ride("B-3300", "Old Mill", "cargo", 15));

    @Test
    @DisplayName("findStation returns a full box for a docked bike and an empty box for a retired one")
    void findStation() {
        assertEquals(Optional.of("Riverside"), lookup.findStation("B-1042"),
                "findStation(\"B-1042\") should hold \"Riverside\". Wrap docks.get(bikeId) with Optional.ofNullable");
        assertTrue(lookup.findStation("B-0007").isEmpty(),
                "findStation(\"B-0007\") should be empty because B-0007 is not in the map");
    }

    @Test
    @DisplayName("stationOrUnknown gives the station, or unknown for a retired bike")
    void stationOrUnknown() {
        assertEquals("Harbor", lookup.stationOrUnknown("B-2001"),
                "stationOrUnknown(\"B-2001\") should be \"Harbor\"");
        assertEquals("unknown", lookup.stationOrUnknown("B-0007"),
                "stationOrUnknown(\"B-0007\") should be \"unknown\". Use findStation(bikeId).orElse(\"unknown\")");
    }

    @Test
    @DisplayName("longestRide finds the ride with the most minutes")
    void longestRide() {
        assertEquals(Optional.of(RIDES.get(1)), lookup.longestRide(RIDES),
                "longestRide should hold the 80 minute ride on B-1042. Use stream().max(...) comparing minutes");
        assertTrue(lookup.longestRide(List.of()).isEmpty(),
                "longestRide of an empty list should be an empty Optional");
    }

    @Test
    @DisplayName("longestRideLabel describes the longest ride, or says no rides")
    void longestRideLabel() {
        assertEquals("B-1042: 80 min", lookup.longestRideLabel(RIDES),
                "The label should be \"B-1042: 80 min\": the bike id, a colon, a space, the minutes and \" min\"");
        assertEquals("no rides", lookup.longestRideLabel(List.of()),
                "With no rides the label should be \"no rides\". Use .map(...).orElse(\"no rides\")");
    }
}
