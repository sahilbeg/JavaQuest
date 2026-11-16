package jq.m08.apidesign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RentalApiTest {

    static final String NOT_FOUND_TYPE = "https://api.spoke.example/problems/rental-not-found";
    static final String INVALID_ID_TYPE = "https://api.spoke.example/problems/invalid-rental-id";

    @Autowired MockMvc mvc;

    /** Reads one value from the JSON body, or null when the body or the field is missing. */
    private static Object json(MvcResult r, String path) throws Exception {
        try {
            return JsonPath.read(r.getResponse().getContentAsString(), path);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The top-level fields of the JSON body, or an empty map when there is no JSON object. */
    private static Map<?, ?> fields(MvcResult r) throws Exception {
        Object body = json(r, "$");
        return body instanceof Map<?, ?> map ? map : Map.of();
    }

    private static void assertProblemJson(MvcResult r) {
        String contentType = r.getResponse().getContentType();
        assertNotNull(contentType, "An error response should have a Content-Type header");
        assertTrue(MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(MediaType.parseMediaType(contentType)),
                "An error should be sent as application/problem+json, but was " + contentType
                        + ". Return a ProblemDetail from the handler.");
    }

    @Test
    @DisplayName("v1 keeps its old shape: the fare is a flat fareCents number")
    void v1KeepsFlatFare() throws Exception {
        MvcResult r = mvc.perform(get("/api/v1/rentals/rental-17")).andReturn();
        assertEquals(200, r.getResponse().getStatus(), "GET /api/v1/rentals/rental-17 should answer 200");
        assertEquals("rental-17", json(r, "$.id"), "v1 should return the rental id");
        assertEquals("B-1042", json(r, "$.bikeId"), "v1 should return the bike id");
        assertEquals(460, json(r, "$.fareCents"), "v1 should return the fare as fareCents: 460");
        assertFalse(fields(r).containsKey("fare"),
                "v1 must not grow a fare object. Partners wrote code for the old shape, so v1 stays as it was.");
    }

    @Test
    @DisplayName("v1 tells callers it is deprecated and when it will be switched off")
    void v1SendsDeprecationHeaders() throws Exception {
        MvcResult r = mvc.perform(get("/api/v1/rentals/rental-17")).andReturn();
        assertEquals("@1793491200", r.getResponse().getHeader("Deprecation"),
                "v1 should send the header Deprecation: @1793491200 (use the DEPRECATION constant)");
        assertEquals("Wed, 30 Jun 2027 23:59:59 GMT", r.getResponse().getHeader("Sunset"),
                "v1 should send the header Sunset: Wed, 30 Jun 2027 23:59:59 GMT (use the SUNSET constant)");
    }

    @Test
    @DisplayName("v2 sends the fare as an object with amountCents and currency")
    void v2HasFareObject() throws Exception {
        MvcResult r = mvc.perform(get("/api/v2/rentals/rental-17")).andReturn();
        assertEquals(200, r.getResponse().getStatus(), "GET /api/v2/rentals/rental-17 should answer 200");
        assertEquals("rental-17", json(r, "$.id"), "v2 should return the rental id");
        assertEquals("B-1042", json(r, "$.bikeId"), "v2 should return the bike id");
        assertEquals(460, json(r, "$.fare.amountCents"), "v2 should return fare.amountCents: 460. Build the Fare in TODO 2.");
        assertEquals("USD", json(r, "$.fare.currency"), "v2 should return fare.currency: USD");
        assertFalse(fields(r).containsKey("fareCents"), "v2 must not contain the old fareCents field");
    }

    @Test
    @DisplayName("v2 is the current version, so it sends no Deprecation header")
    void v2IsNotDeprecated() throws Exception {
        MvcResult r = mvc.perform(get("/api/v2/rentals/rental-18")).andReturn();
        assertEquals(200, r.getResponse().getStatus(), "GET /api/v2/rentals/rental-18 should answer 200");
        assertNull(r.getResponse().getHeader("Deprecation"), "Only v1 is deprecated. v2 should not send a Deprecation header.");
    }

    @Test
    @DisplayName("An unknown rental on v2 answers a 404 problem with a stable type")
    void v2UnknownRentalIsAProblem() throws Exception {
        MvcResult r = mvc.perform(get("/api/v2/rentals/rental-99")).andReturn();
        assertEquals(404, r.getResponse().getStatus(), "GET /api/v2/rentals/rental-99 should answer 404 Not Found");
        assertProblemJson(r);
        assertEquals(NOT_FOUND_TYPE, json(r, "$.type"), "The problem type should be " + NOT_FOUND_TYPE);
        assertEquals("Rental not found", json(r, "$.title"), "The problem title should be \"Rental not found\"");
        assertEquals(404, json(r, "$.status"), "The problem body should repeat the status, 404");
        Object detail = json(r, "$.detail");
        assertTrue(detail instanceof String text && text.contains("rental-99"),
                "The detail should name the missing id, rental-99, but was " + detail);
        assertEquals("/api/v2/rentals/rental-99", json(r, "$.instance"),
                "The instance should be the request path, from request.getRequestURI()");
        assertEquals("rental-99", json(r, "$.rentalId"),
                "The problem should carry the extension field rentalId, so partners can read it in code");
    }

    @Test
    @DisplayName("An unknown rental on v1 answers the same problem type as v2")
    void v1UnknownRentalIsTheSameProblem() throws Exception {
        MvcResult r = mvc.perform(get("/api/v1/rentals/rental-99")).andReturn();
        assertEquals(404, r.getResponse().getStatus(), "GET /api/v1/rentals/rental-99 should answer 404 Not Found");
        assertProblemJson(r);
        assertEquals(NOT_FOUND_TYPE, json(r, "$.type"), "Both versions should use the same problem type, " + NOT_FOUND_TYPE);
        assertEquals("/api/v1/rentals/rental-99", json(r, "$.instance"), "The instance should be the v1 request path");
        assertEquals("rental-99", json(r, "$.rentalId"), "The problem should carry the extension field rentalId");
    }

    @Test
    @DisplayName("A malformed rental id answers a 400 problem of type invalid-rental-id")
    void malformedIdIsABadRequest() throws Exception {
        MvcResult r = mvc.perform(get("/api/v2/rentals/B-1042")).andReturn();
        assertEquals(400, r.getResponse().getStatus(),
                "B-1042 is a bike id, not a rental id, so the API should answer 400 Bad Request");
        assertProblemJson(r);
        assertEquals(INVALID_ID_TYPE, json(r, "$.type"), "The problem type should be " + INVALID_ID_TYPE);
        assertEquals("Invalid rental id", json(r, "$.title"), "The problem title should be \"Invalid rental id\"");
        assertEquals("/api/v2/rentals/B-1042", json(r, "$.instance"), "The instance should be the request path");
    }
}
