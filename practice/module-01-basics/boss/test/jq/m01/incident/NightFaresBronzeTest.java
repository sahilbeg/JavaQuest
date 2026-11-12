package jq.m01.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NightFaresBronzeTest {

    @Test
    @DisplayName("Bronze: a ride from 23:50 to 00:20 lasts 30 minutes")
    void acrossMidnight() {
        assertEquals(30, NightFares.minutesBetween(1430, 20), "23:50 (1430) to 00:20 (20) should be 30 minutes");
    }

    @Test
    @DisplayName("Bronze: a normal ride from 10:00 to 10:15 still lasts 15 minutes")
    void normalRide() {
        assertEquals(15, NightFares.minutesBetween(600, 615), "10:00 (600) to 10:15 (615) should be 15 minutes");
    }
}
