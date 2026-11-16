package jq.m08.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.method.HandlerMethod;

// Starts only the web layer (RentalController), then adds the real RentalService and a clock frozen at 08:00 UTC.
@WebMvcTest(RentalController.class)
@Import(RentalService.class)
class RentalControllerTest {

    static final String EIGHT_AM = "2026-11-16T08:00:00Z";

    @TestConfiguration
    static class FrozenClock {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse(EIGHT_AM), ZoneOffset.UTC);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired RentalService service;

    @BeforeEach
    void startEmpty() {
        service.clear();
    }

    @Test
    @DisplayName("POST /rentals starts a rental and answers 201 Created with the new rental as JSON")
    void postCreatesRental() throws Exception {
        MvcResult r = start("r-17", "B-1042", "harbor");

        assertHandledByController(r, "POST /rentals", "@PostMapping");
        assertEquals(201, r.getResponse().getStatus(), "POST /rentals should answer 201 Created");
        assertEquals("rental-1", json(r, "$.id"), "the first rental should get the id rental-1");
        assertEquals("r-17", json(r, "$.riderId"), "riderId should come from the request body. Did you add @RequestBody?");
        assertEquals("B-1042", json(r, "$.bikeId"), "bikeId should come from the request body");
        assertEquals("harbor", json(r, "$.startStation"), "startStation should be the station from the request body");
        assertEquals(EIGHT_AM, json(r, "$.startedAt"), "startedAt should be the time on the test clock");
        assertNull(json(r, "$.endedAt"), "a new rental has not ended yet, so endedAt should be null");
    }

    @Test
    @DisplayName("POST /rentals puts the new rental's address in the Location header")
    void postSetsLocation() throws Exception {
        start("r-17", "B-1042", "harbor");
        MvcResult r = start("r-22", "B-2001", "uptown");

        assertHandledByController(r, "POST /rentals", "@PostMapping");
        String location = r.getResponse().getHeader("Location");
        assertNotNull(location, "a 201 Created answer should have a Location header. Try ResponseEntity.created(uri)");
        assertTrue(location.endsWith("/rentals/rental-2"),
                "the Location header should point at /rentals/rental-2 for the second rental, but it was " + location);
    }

    @Test
    @DisplayName("GET /rentals/{id} answers 200 OK with the rental that has that id")
    void getFindsRental() throws Exception {
        start("r-17", "B-1042", "harbor");
        start("r-22", "B-2001", "uptown");

        MvcResult r = send(get("/rentals/rental-2"));

        assertHandledByController(r, "GET /rentals/{id}", "@GetMapping(\"/{id}\")");
        assertEquals(200, r.getResponse().getStatus(), "GET /rentals/rental-2 should answer 200 OK");
        assertEquals("rental-2", json(r, "$.id"), "GET /rentals/rental-2 should return rental-2. Check your @PathVariable");
        assertEquals("B-2001", json(r, "$.bikeId"), "rental-2 was started with bike B-2001");
    }

    @Test
    @DisplayName("GET /rentals/{id} answers 404 Not Found for an id that does not exist")
    void getUnknownIs404() throws Exception {
        start("r-17", "B-1042", "harbor");

        MvcResult r = send(get("/rentals/rental-99"));

        assertHandledByController(r, "GET /rentals/{id}", "@GetMapping(\"/{id}\")");
        assertEquals(404, r.getResponse().getStatus(),
                "GET /rentals/rental-99 should answer 404 Not Found. Try throwing new ResponseStatusException(HttpStatus.NOT_FOUND, ...)");
    }

    @Test
    @DisplayName("GET /rentals?riderId=r-17 lists only the rentals of rider r-17")
    void listFiltersByRider() throws Exception {
        start("r-17", "B-1042", "harbor");
        start("r-22", "B-2001", "uptown");
        start("r-17", "B-3003", "depot");

        MvcResult r = send(get("/rentals").param("riderId", "r-17"));

        assertHandledByController(r, "GET /rentals", "@GetMapping");
        assertEquals(200, r.getResponse().getStatus(), "GET /rentals?riderId=r-17 should answer 200 OK");
        List<String> ids = json(r, "$[*].id");
        assertEquals(List.of("rental-1", "rental-3"), ids,
                "only rentals 1 and 3 belong to r-17. Read the filter with @RequestParam and call service.forRider");
    }

    @Test
    @DisplayName("GET /rentals without riderId lists every rental")
    void listWithoutRiderReturnsAll() throws Exception {
        start("r-17", "B-1042", "harbor");
        start("r-22", "B-2001", "uptown");
        start("r-17", "B-3003", "depot");

        MvcResult r = send(get("/rentals"));

        assertHandledByController(r, "GET /rentals", "@GetMapping");
        assertEquals(200, r.getResponse().getStatus(),
                "GET /rentals with no riderId should answer 200 OK. Is your @RequestParam marked required = false?");
        List<String> ids = json(r, "$[*].id");
        assertEquals(List.of("rental-1", "rental-2", "rental-3"), ids,
                "with no riderId, GET /rentals should list every rental. Call service.all() when riderId is null");
    }

    @Test
    @DisplayName("POST /rentals/{id}/end ends the rental, sets endedAt and answers 200 OK")
    void endSetsEndedAt() throws Exception {
        start("r-17", "B-1042", "harbor");

        MvcResult r = send(post("/rentals/rental-1/end"));

        assertHandledByController(r, "POST /rentals/{id}/end", "@PostMapping(\"/{id}/end\")");
        assertEquals(200, r.getResponse().getStatus(), "POST /rentals/rental-1/end should answer 200 OK");
        assertEquals("rental-1", json(r, "$.id"), "the answer should be the rental that just ended");
        assertEquals(EIGHT_AM, json(r, "$.endedAt"),
                "endedAt should be set to the time on the test clock (it is frozen at 08:00, so both times match)");
        assertNotNull(service.find("rental-1").orElseThrow().endedAt(), "the rental should be ended in RentalService too. Call service.end(id)");
    }

    @Test
    @DisplayName("POST /rentals/{id}/end answers 404 Not Found for an id that does not exist")
    void endUnknownIs404() throws Exception {
        MvcResult r = send(post("/rentals/rental-99/end"));

        assertHandledByController(r, "POST /rentals/{id}/end", "@PostMapping(\"/{id}/end\")");
        assertEquals(404, r.getResponse().getStatus(), "ending a rental that does not exist should answer 404 Not Found");
    }

    @Test
    @DisplayName("POST /rentals/{id}/end answers 409 Conflict when the rental has already ended")
    void endTwiceIs409() throws Exception {
        start("r-17", "B-1042", "harbor");
        send(post("/rentals/rental-1/end"));

        MvcResult r = send(post("/rentals/rental-1/end"));

        assertHandledByController(r, "POST /rentals/{id}/end", "@PostMapping(\"/{id}/end\")");
        assertEquals(409, r.getResponse().getStatus(),
                "ending a rental twice should answer 409 Conflict. Catch RentalAlreadyEndedException and use HttpStatus.CONFLICT");
    }

    private MvcResult start(String riderId, String bikeId, String station) throws Exception {
        String body = """
                {"riderId": "%s", "bikeId": "%s", "station": "%s"}
                """.formatted(riderId, bikeId, station);
        return send(post("/rentals").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    // Sends the request. If your controller throws something Spring cannot turn into an answer, the check fails and names it.
    private MvcResult send(MockHttpServletRequestBuilder request) {
        try {
            return mvc.perform(request).andReturn();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            return fail("the request crashed inside the controller with " + root);
        }
    }

    // Reads one value from the JSON body, with a clear failure when the body is empty or the value is missing.
    private static <T> T json(MvcResult r, String path) throws Exception {
        String body = r.getResponse().getContentAsString();
        assertFalse(body.isBlank(), "the answer had an empty body, but it should carry the rental as JSON. Return the rental from your method");
        try {
            return JsonPath.read(body, path);
        } catch (InvalidPathException e) {
            return fail("the answer " + body + " has no value at " + path);
        }
    }

    private static void assertHandledByController(MvcResult r, String request, String annotation) {
        boolean handled = r.getHandler() instanceof HandlerMethod method && method.getBeanType() == RentalController.class;
        assertTrue(handled, "no method in RentalController answered " + request + ". Did you add " + annotation + " to the right method?");
    }
}
