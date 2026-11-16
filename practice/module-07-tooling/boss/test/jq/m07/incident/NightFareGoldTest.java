package jq.m07.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NightFareGoldTest {

    private static long surchargeAt(String instant, ZoneId zone) {
        return new NightFare(Clock.fixed(Instant.parse(instant), zone)).surchargeCents();
    }

    @Test
    @DisplayName("Gold: the surcharge follows the clock it is given, day and night")
    void followsTheGivenClock() {
        assertEquals(50, surchargeAt("2026-11-16T02:00:00Z", ZoneOffset.UTC), "at 02:00 riders pay the 50 cent night surcharge");
        assertEquals(0, surchargeAt("2026-11-16T12:00:00Z", ZoneOffset.UTC), "at 12:00 there is no night surcharge");
    }

    @Test
    @DisplayName("Gold: night starts at 22:00 and ends at 06:00")
    void boundaries() {
        assertEquals(50, surchargeAt("2026-11-16T22:00:00Z", ZoneOffset.UTC), "22:00 is the first minute of the night");
        assertEquals(0, surchargeAt("2026-11-16T21:59:00Z", ZoneOffset.UTC), "21:59 is still day");
        assertEquals(50, surchargeAt("2026-11-16T05:59:00Z", ZoneOffset.UTC), "05:59 is still night");
        assertEquals(0, surchargeAt("2026-11-16T06:00:00Z", ZoneOffset.UTC), "06:00 is the first minute of the day");
    }

    @Test
    @DisplayName("Gold: the clock's time zone decides the local time")
    void usesTheClockZone() {
        // 03:30 in UTC is 22:30 the evening before in New York.
        assertEquals(50, surchargeAt("2026-11-16T03:30:00Z", ZoneId.of("America/New_York")),
                "read the time with the clock you were given, so its zone is used and not the machine's");
        // 17:00 in UTC is 12:00 in New York.
        assertEquals(0, surchargeAt("2026-11-16T17:00:00Z", ZoneId.of("America/New_York")),
                "12:00 in the clock's zone is daytime");
    }
}
