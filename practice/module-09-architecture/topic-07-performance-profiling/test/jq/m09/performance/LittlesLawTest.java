package jq.m09.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LittlesLawTest {

    @Test
    @DisplayName("inFlight multiplies the rate by the latency in seconds, and rounds up")
    void inFlight() {
        assertEquals(50, LittlesLaw.inFlight(200, 250), "200 requests a second x 0.25 s each = 50 in flight at once");
        assertEquals(4, LittlesLaw.inFlight(30, 120), "30 x 0.12 s = 3.6 requests in flight. Round UP to 4: you cannot serve 3.6");
        assertEquals(10, LittlesLaw.inFlight(40, 250), "40 x 0.25 s = exactly 10, so no rounding happens");
        assertEquals(0, LittlesLaw.inFlight(0, 250), "no traffic means nothing in flight");
    }

    @Test
    @DisplayName("threadsNeeded leaves headroom, and maxRequestsPerSecond rounds down")
    void threadsAndThroughput() {
        assertEquals(72, LittlesLaw.threadsNeeded(200, 250, 70),
                "50 in flight, with each thread busy at most 70% of the time: 50 / 0.7 = 71.4, rounded up to 72");
        assertEquals(50, LittlesLaw.threadsNeeded(200, 250, 100), "at 100% utilisation you need exactly the 50 in flight");
        assertEquals(6, LittlesLaw.threadsNeeded(30, 120, 60),
                "30 x 0.12 = 3.6 in flight; 3.6 / 0.6 = 6. Divide the exact 3.6, not the rounded 4 (4 / 0.6 would give 7)");
        assertEquals(80, LittlesLaw.maxRequestsPerSecond(20, 250), "20 threads / 0.25 s per request = 80 requests a second");
        assertEquals(7, LittlesLaw.maxRequestsPerSecond(3, 400),
                "3 / 0.4 s = 7.5 requests a second. Round DOWN to 7: never promise more than you can serve");
    }

    @Test
    @DisplayName("Negative rates or latencies, a zero latency for throughput, and a target outside 1 to 100 are refused")
    void invalidInput() {
        assertThrows(IllegalArgumentException.class, () -> LittlesLaw.inFlight(-1, 250),
                "a negative request rate makes no sense, so inFlight should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> LittlesLaw.inFlight(200, -5),
                "a negative latency makes no sense, so inFlight should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> LittlesLaw.threadsNeeded(200, 250, 0),
                "a 0% target would need infinite threads, so threadsNeeded should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> LittlesLaw.threadsNeeded(200, 250, 101),
                "a thread cannot be busy more than 100% of the time, so a 101% target should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> LittlesLaw.maxRequestsPerSecond(20, 0),
                "a latency of 0 ms would mean infinite throughput, so maxRequestsPerSecond should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> LittlesLaw.maxRequestsPerSecond(-2, 250),
                "a negative thread count makes no sense, so maxRequestsPerSecond should throw IllegalArgumentException");
    }
}
