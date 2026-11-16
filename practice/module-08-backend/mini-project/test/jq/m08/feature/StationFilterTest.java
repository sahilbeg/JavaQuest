package jq.m08.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StationFilterTest extends StationApiTestBase {

    @Test
    @DisplayName("?minBikes=3 keeps only stations with at least 3 available bikes, still sorted by name")
    void minBikes() throws Exception {
        assertEquals(List.of("Harbor", "Riverside"), names(ok("/api/v1/stations?minBikes=3")),
                "only Harbor (3) and Riverside (4) have 3 or more AVAILABLE bikes");
    }

    @Test
    @DisplayName("?minBikes=2&type=EBIKE counts only available e-bikes")
    void ebikesOnly() throws Exception {
        assertEquals(List.of("Old Mill"), names(ok("/api/v1/stations?minBikes=2&type=EBIKE")),
                "only Old Mill has 2 AVAILABLE e-bikes. Riverside has 4 bikes but only 1 AVAILABLE e-bike");
    }

    @Test
    @DisplayName("?minBikes=1&type=CARGO skips a cargo bike that is in maintenance")
    void cargo() throws Exception {
        assertEquals(List.of("Riverside"), names(ok("/api/v1/stations?minBikes=1&type=CARGO")),
                "Riverside has an AVAILABLE cargo bike. Uptown's cargo bike is in MAINTENANCE, so it does not count");
    }

    @Test
    @DisplayName("minBikes may be anything from 0 to 12")
    void edges() throws Exception {
        assertEquals(5, names(ok("/api/v1/stations?minBikes=0")).size(), "?minBikes=0 keeps all 5 stations");
        assertEquals(List.of(), names(ok("/api/v1/stations?minBikes=12")),
                "?minBikes=12 is allowed, but no station has 12 AVAILABLE bikes, so the answer is an empty list");
    }

    @Test
    @DisplayName("minBikes outside 0 to 12 answers 400 with an invalid-request problem")
    void minBikesOutOfRange() throws Exception {
        assertInvalid("/api/v1/stations?minBikes=13", "minBikes");
        assertInvalid("/api/v1/stations?minBikes=-1", "minBikes");
    }

    @Test
    @DisplayName("minBikes that is not a whole number answers 400 with an invalid-request problem")
    void minBikesNotANumber() throws Exception {
        assertInvalid("/api/v1/stations?minBikes=abc", "minBikes");
    }

    @Test
    @DisplayName("An unknown bike type answers 400 with an invalid-request problem")
    void unknownType() throws Exception {
        assertInvalid("/api/v1/stations?minBikes=1&type=SCOOTER", "type");
    }
}
