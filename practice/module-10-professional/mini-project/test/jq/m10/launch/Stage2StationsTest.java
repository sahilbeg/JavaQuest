package jq.m10.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

// Stage 2: GET /stations/{id}/availability, the live numbers for Bramford's map.
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:launch-stage2;DB_CLOSE_DELAY=-1",
        "spring.main.banner-mode=off"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class Stage2StationsTest extends LaunchChecks {

    private MvcResult availability(String stationId) throws Exception {
        MvcResult r = getJson("/stations/" + stationId + "/availability");
        assertEquals(200, r.getResponse().getStatus(), "GET /stations/" + stationId + "/availability should answer 200");
        String type = r.getResponse().getContentType();
        assertTrue(type != null && MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(type)),
                "Availability should be JSON, but the content type was " + type);
        return r;
    }

    private void assertNumbers(MvcResult r, int bikes, int ebikes, int docksFree, String why) throws Exception {
        assertEquals(bikes, number(r, "$.bikesAvailable"), "bikesAvailable should be " + bikes + ": " + why);
        assertEquals(ebikes, number(r, "$.ebikesAvailable"), "ebikesAvailable should be " + ebikes + ": " + why);
        assertEquals(docksFree, number(r, "$.docksFree"), "docksFree should be " + docksFree + ": " + why);
    }

    @Test
    @DisplayName("Stage 2: Bramford Central on launch day has 4 bikes, 2 of them e-bikes, and 6 free docks")
    void launchDayNumbers() throws Exception {
        MvcResult r = availability("bramford-central");

        assertEquals("bramford-central", json(r, "$.id"), "id should be the station id");
        assertEquals("Bramford Central", json(r, "$.name"), "name should be the station's name");
        assertEquals(10, number(r, "$.docks"), "Every Bramford station has 10 docks");
        assertNumbers(r, 4, 2, 6, "BR-2041 to BR-2044 are docked there, out of 10 docks");
    }

    @Test
    @DisplayName("Stage 2: the CARGO bike at Market Square takes a dock but is not offered to riders")
    void cargoTakesADock() throws Exception {
        assertNumbers(availability("market-square"), 4, 2, 5,
                "BR-2900 (CARGO) fills a dock, but Bramford does not rent CARGO, so it is not available");
    }

    @Test
    @DisplayName("Stage 2: renting at University and docking at Canal Street moves the numbers at both stations")
    void numbersFollowTheBike() throws Exception {
        String id = startedId("rider-21", "BR-2031", "university");
        assertNumbers(availability("university"), 3, 2, 7, "BR-2031 left University");

        clock.advance(Duration.ofMinutes(5));
        assertEquals(200, endRental(id, "canal-street").getResponse().getStatus(), "Ending the ride should answer 200");

        assertNumbers(availability("canal-street"), 5, 2, 5, "BR-2031 is now docked at Canal Street");
        assertNumbers(availability("university"), 3, 2, 7, "BR-2031 did not come back to University");
    }
}
