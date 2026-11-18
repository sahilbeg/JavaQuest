package jq.m10.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// Stage 6: launch readiness. A readiness probe for Kubernetes, and a per-rider rate limit on POST /rentals.
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:launch-stage6;DB_CLOSE_DELAY=-1",
        "spring.main.banner-mode=off"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class Stage6LaunchReadinessTest extends LaunchChecks {

    private static void assertHealth(MvcResult r, int status, String health, int loaded) throws Exception {
        assertEquals(status, r.getResponse().getStatus(), "/health/ready should answer " + status);
        assertEquals(health, json(r, "$.status"), "status should be \"" + health + "\"");
        assertEquals(loaded, number(r, "$.stationsLoaded"), "stationsLoaded should be " + loaded);
    }

    @Test
    @DisplayName("Stage 6: once the app has loaded Bramford's five stations, /health/ready answers 200 UP")
    void readyWhenLoaded() throws Exception {
        assertHealth(getJson("/health/ready"), 200, "UP", 5);
    }

    @Test
    @DisplayName("Stage 6: /health/ready answers 503 DOWN until every station is loaded; /health/live stays UP")
    void downUntilLoaded() throws Exception {
        StationDirectory stations = new StationDirectory(CityConfig.bramford());
        MockMvc server = MockMvcBuilders.standaloneSetup(new HealthController(stations)).build();

        assertHealth(server.perform(get("/health/ready")).andReturn(), 503, "DOWN", 0);
        assertEquals(200, server.perform(get("/health/live")).andReturn().getResponse().getStatus(),
                "Liveness is about the process, not the data: /health/live should stay 200 while loading");

        stations.load("castle-hill");
        assertHealth(server.perform(get("/health/ready")).andReturn(), 503, "DOWN", 1);

        stations.loadAll();
        assertHealth(server.perform(get("/health/ready")).andReturn(), 200, "UP", 5);
    }

    @Test
    @DisplayName("Stage 6: a rider's 6th POST /rentals within a minute is a 429 with Retry-After; other riders are fine")
    void sixthRequestIsLimited() throws Exception {
        startedId("rider-61", "BR-2001", "castle-hill");
        for (int s = 10; s <= 40; s += 10) {
            clock.set(NINE_AM.plusSeconds(s));
            assertEquals(409, startRental("rider-61", "BR-2003", "castle-hill").getResponse().getStatus(),
                    "rider-61 is riding, so requests 2 to 5 reach the core and get 409");
        }

        clock.set(NINE_AM.plusSeconds(50));
        MvcResult limited = startRental("rider-61", "BR-2002", "castle-hill");

        assertProblem(limited, 429, "rate-limited", "Too many requests");
        assertEquals("10", limited.getResponse().getHeader("Retry-After"),
                "The oldest request (09:00:00) leaves the window at 09:01:00, 10 seconds from 09:00:50");
        assertEquals(201, startRental("rider-62", "BR-2002", "castle-hill").getResponse().getStatus(),
                "The limit is per rider: rider-62 is not slowed down by rider-61, and BR-2002 is still free");
    }

    @Test
    @DisplayName("Stage 6: Retry-After rounds up, and after 60 seconds the oldest request no longer counts")
    void windowSlides() throws Exception {
        startedId("rider-63", "BR-2011", "canal-street");
        for (int i = 2; i <= 5; i++) {
            startRental("rider-63", "BR-2012", "canal-street");
        }

        clock.set(Instant.parse("2026-11-18T09:00:59.400Z"));
        MvcResult limited = startRental("rider-63", "BR-2012", "canal-street");
        assertEquals(429, limited.getResponse().getStatus(), "Five requests at 09:00:00, so the 6th at 09:00:59.4 is 429");
        assertEquals("1", limited.getResponse().getHeader("Retry-After"),
                "0.6 s are left. Round UP to whole seconds: 1, never 0");

        clock.set(Instant.parse("2026-11-18T09:01:00Z"));
        assertProblem(startRental("rider-63", "BR-2012", "canal-street"), 409, "active-rental-exists",
                "Rider already has an active rental");
    }
}
