package jq.m09.layered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;
import java.util.Set;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Starts the whole app and sends requests through MockMvc: no network, no port.
// Every test uses its own bike, so the tests do not disturb each other.
@SpringBootTest(properties = "spring.main.banner-mode=off")
@AutoConfigureMockMvc
class RentalApiTest {

    static final String PROBLEMS = "https://api.spoke.example/problems/";
    static final Set<String> RENTAL_FIELDS = Set.of("id", "riderId", "bikeId", "stationId", "status");

    @Autowired MockMvc mvc;

    private MvcResult start(String riderId, String bikeId) throws Exception {
        String body = """
                {"riderId":"%s","bikeId":"%s","stationId":"harbor","paymentToken":"tok_visa_4242"}
                """.formatted(riderId, bikeId);
        return mvc.perform(post("/rentals").contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
    }

    /** Reads one value from the JSON body, or null when the body or the field is missing. */
    private static Object json(MvcResult r, String path) throws Exception {
        try {
            return JsonPath.read(r.getResponse().getContentAsString(), path);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The top-level field names of the JSON body, or an empty set when there is no JSON object. */
    private static Set<?> fields(MvcResult r) throws Exception {
        Object body = json(r, "$");
        return body instanceof Map<?, ?> map ? map.keySet() : Set.of();
    }

    private static void assertProblem(MvcResult r, int status, String type) throws Exception {
        assertEquals(status, r.getResponse().getStatus(), "The API should answer " + status
                + ". Throw the domain exception in the service and map it in ApiErrors.");
        String contentType = r.getResponse().getContentType();
        assertTrue(contentType != null
                        && MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(MediaType.parseMediaType(contentType)),
                "An error should be sent as application/problem+json, but was " + contentType
                        + ". Return a ProblemDetail from the handler in ApiErrors.");
        assertEquals(PROBLEMS + type, json(r, "$.type"), "The problem type should be " + PROBLEMS + type);
    }

    @Test
    @DisplayName("POST /rentals answers 201 Created with a Location header and the new rental")
    void startAnswers201WithLocation() throws Exception {
        MvcResult r = start("rider-7", "B-1042");

        assertEquals(201, r.getResponse().getStatus(), "POST /rentals should answer 201 Created");
        Object id = json(r, "$.id");
        assertTrue(id instanceof String text && text.matches("rental-\\d+"),
                "The body should hold the new rental's id, like rental-1, but was " + id);
        assertEquals("/rentals/" + id, r.getResponse().getHeader("Location"),
                "The Location header should point at the new rental: /rentals/" + id);
        assertEquals("rider-7", json(r, "$.riderId"), "riderId should come from the request");
        assertEquals("B-1042", json(r, "$.bikeId"), "bikeId should come from the request");
        assertEquals("harbor", json(r, "$.stationId"), "stationId should come from the request");
        assertEquals("ACTIVE", json(r, "$.status"), "A new rental should have status ACTIVE");
    }

    @Test
    @DisplayName("The rental JSON never contains the payment token or other internal fields")
    void responseHidesInternalFields() throws Exception {
        MvcResult r = start("rider-8", "B-2001");

        assertEquals(201, r.getResponse().getStatus(), "POST /rentals should answer 201 Created");
        assertFalse(fields(r).contains("paymentToken"),
                "The JSON must not contain paymentToken. Return a RentalResponse, never the Rental entity.");
        assertEquals(RENTAL_FIELDS, fields(r),
                "The rental JSON should have exactly the fields " + RENTAL_FIELDS + ", but had " + fields(r));
    }

    @Test
    @DisplayName("GET /rentals/{id} returns the same rental as a RentalResponse")
    void getReturnsTheRental() throws Exception {
        MvcResult created = start("rider-9", "B-3007");
        String location = created.getResponse().getHeader("Location");
        assertNotNull(location, "POST /rentals should send a Location header");

        MvcResult r = mvc.perform(get(location)).andReturn();

        assertEquals(200, r.getResponse().getStatus(), "GET " + location + " should answer 200");
        assertEquals(json(created, "$.id"), json(r, "$.id"), "GET should return the rental that POST created");
        assertEquals("B-3007", json(r, "$.bikeId"), "GET should return the rental's bikeId");
        assertEquals(RENTAL_FIELDS, fields(r),
                "GET should return exactly the fields " + RENTAL_FIELDS + ", but had " + fields(r));
    }

    @Test
    @DisplayName("GET of an unknown rental answers a 404 rental-not-found problem")
    void unknownRentalIs404Problem() throws Exception {
        MvcResult r = mvc.perform(get("/rentals/rental-999")).andReturn();

        assertProblem(r, 404, "rental-not-found");
        assertEquals("Rental not found", json(r, "$.title"), "The problem title should be \"Rental not found\"");
        assertEquals("rental-999", json(r, "$.rentalId"), "The problem should carry the property rentalId");
    }

    @Test
    @DisplayName("Renting a bike that is already out answers a 409 bike-unavailable problem")
    void busyBikeIs409Problem() throws Exception {
        assertEquals(201, start("rider-10", "B-4040").getResponse().getStatus(), "The first rental of B-4040 should work");

        MvcResult r = start("rider-11", "B-4040");

        assertProblem(r, 409, "bike-unavailable");
        assertEquals("Bike unavailable", json(r, "$.title"), "The problem title should be \"Bike unavailable\"");
        assertEquals("B-4040", json(r, "$.bikeId"), "The problem should carry the property bikeId");
    }

    @Test
    @DisplayName("Ending a rental answers 200 with status ENDED, and frees the bike for the next rider")
    void endFreesTheBike() throws Exception {
        MvcResult created = start("rider-12", "B-5050");
        Object id = json(created, "$.id");
        assertNotNull(id, "POST /rentals should return the new rental's id");

        MvcResult ended = mvc.perform(post("/rentals/" + id + "/end")).andReturn();

        assertEquals(200, ended.getResponse().getStatus(), "POST /rentals/{id}/end should answer 200");
        assertEquals("ENDED", json(ended, "$.status"), "An ended rental should have status ENDED");
        assertEquals(RENTAL_FIELDS, fields(ended), "The ended rental should have exactly the fields " + RENTAL_FIELDS);
        assertEquals(201, start("rider-13", "B-5050").getResponse().getStatus(),
                "After the ride ends, another rider should be able to rent B-5050");
    }

    @Test
    @DisplayName("Ending a rental twice answers a 409 rental-already-ended problem")
    void endTwiceIs409Problem() throws Exception {
        Object id = json(start("rider-14", "B-6060"), "$.id");
        assertNotNull(id, "POST /rentals should return the new rental's id");
        mvc.perform(post("/rentals/" + id + "/end")).andReturn();

        MvcResult r = mvc.perform(post("/rentals/" + id + "/end")).andReturn();

        assertProblem(r, 409, "rental-already-ended");
        assertEquals(id, json(r, "$.rentalId"), "The problem should carry the property rentalId");
    }

    @Test
    @DisplayName("A request without a bikeId is rejected with 400 at the edge, before any rule runs")
    void blankBikeIdIs400() throws Exception {
        String body = """
                {"riderId":"rider-15","stationId":"harbor","paymentToken":"tok_visa_4242"}
                """;
        MvcResult r = mvc.perform(post("/rentals").contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();

        assertEquals(400, r.getResponse().getStatus(),
                "A missing bikeId should answer 400 Bad Request. Put @Valid before @RequestBody.");
    }
}
