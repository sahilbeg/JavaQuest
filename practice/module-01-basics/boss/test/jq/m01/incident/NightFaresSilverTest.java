package jq.m01.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NightFaresSilverTest {

    @Test
    @DisplayName("Silver: a non-member pays the unlock fee plus every minute")
    void nonMember() {
        assertEquals(5.50, NightFares.fare(1430, 20, "classic", false), 0.001,
                "30 classic minutes for a non-member: 1.00 + 30 x 0.15 = 5.50");
    }

    @Test
    @DisplayName("Silver: a member rides the first 30 minutes free")
    void memberShortRide() {
        assertEquals(0.0, NightFares.fare(600, 620, "ebike", true), 0.001,
                "a 20 minute member ride should be free, with no unlock fee");
    }

    @Test
    @DisplayName("Silver: a member pays only for minutes after the first 30")
    void memberLongRide() {
        assertEquals(3.00, NightFares.fare(1430, 30, "ebike", true), 0.001,
                "40 e-bike minutes for a member: 10 paid minutes x 0.30 = 3.00");
    }

    @Test
    @DisplayName("Silver: an unknown bike type costs -1")
    void unknownType() {
        assertEquals(-1, NightFares.fare(600, 610, "tandem", false), 0.001, "unknown bike types should return -1");
    }
}
