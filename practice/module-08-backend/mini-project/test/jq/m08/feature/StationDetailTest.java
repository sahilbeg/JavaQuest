package jq.m08.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class StationDetailTest extends StationApiTestBase {

    @Test
    @DisplayName("GET /api/v1/stations/old-mill returns that one station")
    void oneStation() throws Exception {
        MvcResult r = ok("/api/v1/stations/old-mill");
        assertEquals("old-mill", json(r, "$.id"), "the answer should be the station with id old-mill");
        assertEquals("Old Mill", json(r, "$.name"), "Old Mill's name should be \"Old Mill\"");
        assertEquals(12, json(r, "$.docks"), "Old Mill has 12 docks");
        assertEquals(2, json(r, "$.bikesAvailable"), "Old Mill has 2 AVAILABLE bikes, both e-bikes");
        assertEquals(2, json(r, "$.ebikesAvailable"), "both AVAILABLE bikes at Old Mill are e-bikes");
        assertEquals(9, json(r, "$.docksFree"), "Old Mill has 3 docked bikes (2 AVAILABLE + 1 MAINTENANCE), so 9 free docks");
    }

    @Test
    @DisplayName("An unknown station answers 404 with a station-not-found problem")
    void unknownStation() throws Exception {
        MvcResult r = call("/api/v1/stations/moon");
        assertEquals(404, r.getResponse().getStatus(), "there is no station \"moon\", so the answer should be 404 Not Found");
        assertEquals(PROBLEM_JSON, r.getResponse().getContentType(),
                "a 404 should carry a ProblemDetail, whose Content-Type is application/problem+json");
        assertEquals("https://api.spoke.example/problems/station-not-found", json(r, "$.type"),
                "the problem type should be the station-not-found URI");
        assertEquals("Station not found", json(r, "$.title"), "the title should be \"Station not found\"");
        assertEquals(404, json(r, "$.status"), "the body should repeat the status 404");
        assertEquals("/api/v1/stations/moon", json(r, "$.instance"), "instance should be the path that was asked for");
        assertEquals("moon", json(r, "$.stationId"), "the extra property stationId should hold the id that was not found");
        Object detail = json(r, "$.detail");
        assertTrue(detail instanceof String text && text.contains("moon"),
                "detail should be a sentence for a human that mentions the id \"moon\"");
    }
}
