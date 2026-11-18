package jq.m10.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

// Stage 3: every error is a Problem Details answer with the status and type from the README's error catalog.
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:launch-stage3;DB_CLOSE_DELAY=-1",
        "spring.main.banner-mode=off"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class Stage3ErrorsTest extends LaunchChecks {

    /** The keys of the problem's errors map, or an empty set when there is none. */
    private static Set<?> errorFields(MvcResult r) throws Exception {
        Object errors = json(r, "$.errors");
        return errors instanceof Map<?, ?> map ? map.keySet() : Set.of();
    }

    @Test
    @DisplayName("Stage 3: an unknown rental is a 404 rental-not-found problem")
    void unknownRental() throws Exception {
        assertProblem(getJson("/rentals/rental-999"), 404, "rental-not-found", "Rental not found");
    }

    @Test
    @DisplayName("Stage 3: a Riverton station in Bramford is a 404 station-not-found problem, on GET and on POST")
    void foreignStation() throws Exception {
        assertProblem(getJson("/stations/harbor/availability"), 404, "station-not-found", "Station not found");
        assertProblem(startRental("rider-31", "BR-2001", "harbor"), 404, "station-not-found", "Station not found");
    }

    @Test
    @DisplayName("Stage 3: a Riverton bike in Bramford is a 404 bike-not-found problem")
    void foreignBike() throws Exception {
        assertProblem(startRental("rider-32", "B-1042", "castle-hill"), 404, "bike-not-found", "Bike not found");
    }

    @Test
    @DisplayName("Stage 3: renting the CARGO bike in Bramford is a 422 bike-type-not-offered problem")
    void cargoNotOffered() throws Exception {
        assertProblem(startRental("rider-33", "BR-2900", "market-square"), 422, "bike-type-not-offered",
                "Bike type not offered");
    }

    @Test
    @DisplayName("Stage 3: a bike that is out is 409 bike-unavailable; a rider already riding is 409 active-rental-exists")
    void conflicts() throws Exception {
        startedId("rider-34", "BR-2002", "castle-hill");

        assertProblem(startRental("rider-35", "BR-2002", "castle-hill"), 409, "bike-unavailable", "Bike unavailable");
        assertProblem(startRental("rider-34", "BR-2004", "castle-hill"), 409, "active-rental-exists",
                "Rider already has an active rental");
    }

    @Test
    @DisplayName("Stage 3: ending a rental twice is a 409 rental-already-ended problem")
    void endTwice() throws Exception {
        String id = startedId("rider-36", "BR-2011", "canal-street");
        assertEquals(200, endRental(id, "canal-street").getResponse().getStatus(), "The first end should answer 200");

        assertProblem(endRental(id, "canal-street"), 409, "rental-already-ended", "Rental already ended");
    }

    @Test
    @DisplayName("Stage 3: a missing or blank field is a 400 invalid-request problem with an errors entry per field")
    void validation() throws Exception {
        MvcResult start = mvc.perform(post("/rentals").contentType(MediaType.APPLICATION_JSON)
                .content("{\"bikeId\":\"  \",\"stationId\":\"castle-hill\"}")).andReturn();
        assertProblem(start, 400, "invalid-request", "Invalid request");
        assertEquals(Set.of("bikeId", "riderId"), errorFields(start),
                "errors should have one entry per bad field: riderId is missing and bikeId is blank");

        MvcResult end = endRental("rental-1", " ");
        assertProblem(end, 400, "invalid-request", "Invalid request");
        assertEquals(Set.of("stationId"), errorFields(end), "errors should have one entry, stationId, for a blank stationId");
    }
}
