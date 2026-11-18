package jq.m10.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Shared helpers for the stage checks. Each stage class starts its own app, with its own H2 database
 * and a fresh launch-day fleet, and sends requests through MockMvc: no network, no port.
 * The clock starts every test at 2026-11-18T09:00:00Z.
 */
abstract class LaunchChecks {

    static final String PROBLEMS = "https://api.spoke.example/problems/";
    static final Instant NINE_AM = Instant.parse("2026-11-18T09:00:00Z");

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    @BeforeEach
    void resetClock() {
        clock.set(NINE_AM);
    }

    MvcResult startRental(String riderId, String bikeId, String stationId) throws Exception {
        return startRental(riderId, bikeId, stationId, null);
    }

    MvcResult startRental(String riderId, String bikeId, String stationId, String idempotencyKey) throws Exception {
        String body = """
                {"riderId":"%s","bikeId":"%s","stationId":"%s"}
                """.formatted(riderId, bikeId, stationId);
        MockHttpServletRequestBuilder request = post("/rentals").contentType(MediaType.APPLICATION_JSON).content(body);
        if (idempotencyKey != null) {
            request.header("Idempotency-Key", idempotencyKey);
        }
        return mvc.perform(request).andReturn();
    }

    MvcResult endRental(Object rentalId, String stationId) throws Exception {
        return mvc.perform(post("/rentals/" + rentalId + "/end").contentType(MediaType.APPLICATION_JSON)
                .content("{\"stationId\":\"" + stationId + "\"}")).andReturn();
    }

    MvcResult getJson(String path) throws Exception {
        return mvc.perform(get(path)).andReturn();
    }

    /** Starts a rental and returns its id, or fails with a clear message. */
    String startedId(String riderId, String bikeId, String stationId) throws Exception {
        MvcResult r = startRental(riderId, bikeId, stationId);
        assertEquals(201, r.getResponse().getStatus(),
                "POST /rentals for " + bikeId + " at " + stationId + " should answer 201 Created (stage 1)");
        Object id = json(r, "$.id");
        assertTrue(id instanceof String, "POST /rentals should return the new rental's id in the JSON (stage 1)");
        return (String) id;
    }

    /** Reads one value from the JSON body, or null when the body or the field is missing. */
    static Object json(MvcResult r, String path) throws Exception {
        try {
            return JsonPath.read(r.getResponse().getContentAsString(), path);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The top-level field names of the JSON body, or an empty set when there is no JSON object. */
    static Set<?> fields(MvcResult r) throws Exception {
        Object body = json(r, "$");
        return body instanceof Map<?, ?> map ? map.keySet() : Set.of();
    }

    static int number(MvcResult r, String path) throws Exception {
        Object value = json(r, path);
        return value instanceof Number n ? n.intValue() : -1;
    }

    static void assertProblem(MvcResult r, int status, String type, String title) throws Exception {
        assertEquals(status, r.getResponse().getStatus(), "The API should answer " + status + " (" + type + ")."
                + " Map the exception in LaunchProblems.");
        String contentType = r.getResponse().getContentType();
        assertTrue(contentType != null
                        && MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(MediaType.parseMediaType(contentType)),
                "An error should be sent as application/problem+json, but was " + contentType
                        + ". Return a ProblemDetail from LaunchProblems.");
        assertEquals(PROBLEMS + type, json(r, "$.type"), "The problem type should be " + PROBLEMS + type);
        assertEquals(title, json(r, "$.title"), "The problem title should be \"" + title + "\"");
        assertEquals(status, number(r, "$.status"), "The problem's status field should be " + status);
    }
}
