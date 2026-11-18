package jq.m10.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MvcResult;

// Stage 4: the Idempotency-Key header on POST /rentals. One tap, one rental, however often the app retries.
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:launch-stage4;DB_CLOSE_DELAY=-1",
        "spring.main.banner-mode=off"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class Stage4IdempotencyTest extends LaunchChecks {

    @Test
    @DisplayName("Stage 4: a retry with the same key and body answers 201 with the same rental, and rents one bike")
    void retryGetsTheSameRental() throws Exception {
        MvcResult first = startRental("rider-41", "BR-2021", "market-square", "tap-41");
        clock.advance(Duration.ofSeconds(2));
        MvcResult retry = startRental("rider-41", "BR-2021", "market-square", "tap-41");

        assertEquals(201, first.getResponse().getStatus(), "The first request should answer 201");
        assertEquals(201, retry.getResponse().getStatus(),
                "The retry should answer 201 too, the same answer as the first time. Pass the Idempotency-Key "
                        + "header to the core.");
        assertNotNull(json(first, "$.id"), "The first answer should hold the rental's id");
        assertEquals(json(first, "$.id"), json(retry, "$.id"), "The retry should return the same rental id");
        assertEquals(first.getResponse().getHeader("Location"), retry.getResponse().getHeader("Location"),
                "The retry should send the same Location header");
        assertEquals("2026-11-18T09:00:00Z", json(retry, "$.startedAt"),
                "The retry returns the first rental, which started at 09:00:00, not a new one");
        assertEquals(3, number(getJson("/stations/market-square/availability"), "$.bikesAvailable"),
                "Only one bike should have left market-square (4 - 1 = 3)");
    }

    @Test
    @DisplayName("Stage 4: the same key with a different body is a 422 idempotency-key-reused problem")
    void sameKeyDifferentBody() throws Exception {
        assertEquals(201, startRental("rider-42", "BR-2031", "university", "tap-42").getResponse().getStatus(),
                "The first request should answer 201");

        MvcResult r = startRental("rider-42", "BR-2033", "university", "tap-42");

        assertProblem(r, 422, "idempotency-key-reused", "Idempotency key reused");
        assertEquals(201, startRental("rider-43", "BR-2033", "university").getResponse().getStatus(),
                "BR-2033 must not have been rented by the refused request, so rider-43 can take it");
    }

    @Test
    @DisplayName("Stage 4: keys belong to one rider: two riders using the same key text get two rentals")
    void keysArePerRider() throws Exception {
        MvcResult a = startRental("rider-44", "BR-2041", "bramford-central", "tap-shared");
        MvcResult b = startRental("rider-45", "BR-2042", "bramford-central", "tap-shared");

        assertEquals(201, a.getResponse().getStatus(), "rider-44's request should answer 201");
        assertEquals(201, b.getResponse().getStatus(), "rider-45's request should answer 201 too");
        assertNotEquals(json(a, "$.id"), json(b, "$.id"), "Two riders should get two different rentals");
        assertEquals("BR-2042", json(b, "$.bikeId"), "rider-45 should get BR-2042, not rider-44's rental");
    }
}
