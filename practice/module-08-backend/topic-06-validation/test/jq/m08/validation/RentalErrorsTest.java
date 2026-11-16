package jq.m08.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Starts only the web layer (RentalController and your SpokeErrorHandler), then adds the real BikeCatalog.
@WebMvcTest(RentalController.class)
@Import(BikeCatalog.class)
class RentalErrorsTest {

    static final String PROBLEMS = "https://api.spoke.example/problems/";

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("A correct request still starts a rental with 201 Created")
    void validRequestStillWorks() throws Exception {
        MvcResult r = start("r-17", "B-1042", "harbor");

        assertEquals(201, r.getResponse().getStatus(), "a correct request for bike B-1042 should still answer 201 Created");
        assertEquals("B-1042", json(r, "$.bikeId"), "the new rental should carry bike B-1042");
    }

    @Test
    @DisplayName("A bike id like b1042 answers 400 with the title Invalid request, as problem JSON")
    void badBikeIdIs400() throws Exception {
        MvcResult r = startInvalid("r-17", "b1042", "harbor");

        assertEquals(400, r.getResponse().getStatus(),
                "b1042 does not match B-\\d{4}, so the answer should be 400. Did you add @Pattern to bikeId and @Valid to the controller?");
        assertProblemJson(r);
        assertEquals("Invalid request", json(r, "$.title"), "the 400 problem should have the title \"Invalid request\"");
        assertEquals(PROBLEMS + "invalid-request", json(r, "$.type"), "the 400 problem should have type " + PROBLEMS + "invalid-request");
        assertEquals(400, (int) json(r, "$.status"), "the problem's status field should be 400");
    }

    @Test
    @DisplayName("The 400 answer explains the bad bikeId in errors: must look like B-1042")
    void badBikeIdIsListed() throws Exception {
        MvcResult r = startInvalid("r-17", "b1042", "harbor");

        assertEquals(400, r.getResponse().getStatus(), "b1042 is not a valid bike id, so the answer should be 400");
        Map<String, String> errors = json(r, "$.errors");
        assertEquals("must look like B-1042", errors.get("bikeId"),
                "errors.bikeId should hold the @Pattern message. Use @Pattern(regexp = \"B-\\\\d{4}\", message = \"must look like B-1042\")");
    }

    @Test
    @DisplayName("A blank riderId and a missing station each get their own entry in errors")
    void everyBadFieldIsListed() throws Exception {
        MvcResult r = send("{\"riderId\": \"  \", \"bikeId\": \"B-1042\"}");

        assertEquals(400, r.getResponse().getStatus(),
                "a blank riderId and no station should answer 400. Did you add @NotBlank to riderId and station?");
        Map<String, String> errors = json(r, "$.errors");
        assertTrue(errors.containsKey("riderId"), "errors should have an entry for riderId, but it was " + errors);
        assertTrue(errors.containsKey("station"), "errors should have an entry for station, but it was " + errors);
        assertFalse(errors.containsKey("bikeId"), "B-1042 is a good bike id, so errors should not mention bikeId: " + errors);
    }

    @Test
    @DisplayName("An unknown bike answers 404 with the bike-not-found problem type")
    void unknownBikeIs404() throws Exception {
        MvcResult r = start("r-17", "B-9999", "harbor");

        assertEquals(404, r.getResponse().getStatus(), "no bike has the id B-9999, so the answer should be 404 Not Found");
        assertProblemJson(r);
        assertEquals(PROBLEMS + "bike-not-found", json(r, "$.type"), "the 404 problem should have type " + PROBLEMS + "bike-not-found");
        assertEquals("Bike not found", json(r, "$.title"), "the 404 problem should have the title \"Bike not found\"");
        assertEquals(404, (int) json(r, "$.status"), "the problem's status field should be 404");
        String detail = json(r, "$.detail");
        assertTrue(detail != null && detail.contains("B-9999"), "the detail should name the bike the rider typed, B-9999, but it was: " + detail);
        assertEquals("/rentals", json(r, "$.instance"), "instance should be the path that failed, /rentals. Spring fills it in when you return a ProblemDetail");
    }

    @Test
    @DisplayName("A rented bike and a bike in maintenance both answer 409 with the bike-unavailable problem type")
    void unavailableBikeIs409() throws Exception {
        for (String bikeId : new String[] {"B-2001", "B-3003"}) {
            MvcResult r = start("r-17", bikeId, "harbor");

            assertEquals(409, r.getResponse().getStatus(), bikeId + " cannot be rented right now, so the answer should be 409 Conflict");
            assertProblemJson(r);
            assertEquals(PROBLEMS + "bike-unavailable", json(r, "$.type"), "the 409 problem should have type " + PROBLEMS + "bike-unavailable");
            assertEquals("Bike unavailable", json(r, "$.title"), "the 409 problem should have the title \"Bike unavailable\"");
            String detail = json(r, "$.detail");
            assertTrue(detail != null && detail.contains(bikeId), "the detail should name bike " + bikeId + ", but it was: " + detail);
        }
    }

    @Test
    @DisplayName("A surprise bug answers 500 with the detail Something went wrong on our side")
    void surpriseBugIs500() throws Exception {
        MvcResult r = start("r-17", "B-0000", "harbor");

        assertEquals(500, r.getResponse().getStatus(), "an unexpected crash should answer 500 Internal Server Error");
        assertProblemJson(r);
        assertEquals("Something went wrong on our side", json(r, "$.detail"),
                "the 500 problem should have the detail \"Something went wrong on our side\"");
        assertEquals(500, (int) json(r, "$.status"), "the problem's status field should be 500");
    }

    @Test
    @DisplayName("The 500 answer hides class names and package names from the rider")
    void surpriseBugHidesInternals() throws Exception {
        MvcResult r = start("r-17", "B-0000", "harbor");
        String body = r.getResponse().getContentAsString();

        assertEquals(500, r.getResponse().getStatus(), "an unexpected crash should answer 500 Internal Server Error");
        assertFalse(body.contains("Exception"), "the 500 body must not name an exception class, but it was: " + body);
        assertFalse(body.contains("jq.m08"), "the 500 body must not show our package names, but it was: " + body);
        assertFalse(body.contains("DockLink"), "the 500 body must not repeat the exception's message, but it was: " + body);
    }

    @Test
    @DisplayName("A surprise bug is logged once at ERROR level, with the exception attached for the team")
    void surpriseBugIsLogged() {
        Logger logger = (Logger) LoggerFactory.getLogger(SpokeErrorHandler.class);
        ListAppender<ILoggingEvent> logged = new ListAppender<>();
        logged.start();
        logger.addAppender(logged);
        try {
            start("r-17", "B-0000", "harbor");
        } finally {
            logger.detachAppender(logged);
        }

        List<ILoggingEvent> errors = logged.list.stream().filter(event -> event.getLevel() == Level.ERROR).toList();
        assertEquals(1, errors.size(), "the catch-all should log the surprise bug once with log.error, so the team can find it");
        assertNotNull(errors.get(0).getThrowableProxy(),
                "pass the exception itself as the last argument, like log.error(\"Unexpected error\", e), so the stack trace reaches the log");
    }

    @Test
    @DisplayName("Broken JSON still answers 400, not 500")
    void brokenJsonStays400() throws Exception {
        MvcResult r = send("{\"riderId\": \"r-17\", ");

        assertEquals(400, r.getResponse().getStatus(),
                "broken JSON is the app's mistake, so it should stay 400. Keep extending ResponseEntityExceptionHandler, which handles it for you");
    }

    private MvcResult start(String riderId, String bikeId, String station) {
        return send("""
                {"riderId": "%s", "bikeId": "%s", "station": "%s"}
                """.formatted(riderId, bikeId, station));
    }

    // For requests that validation should stop before the bike catalog is ever asked.
    private MvcResult startInvalid(String riderId, String bikeId, String station) {
        try {
            return start(riderId, bikeId, station);
        } catch (AssertionError e) {
            return fail("bike id " + bikeId + " got past validation and reached the bike catalog."
                    + " Add the constraints in StartRentalRequest and @Valid in RentalController");
        }
    }

    // Sends POST /rentals. An exception that no handler answered fails the check with a hint.
    private MvcResult send(String body) {
        try {
            return mvc.perform(post("/rentals").contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            return fail("POST /rentals crashed with " + root.getClass().getSimpleName()
                    + " and nothing turned it into an answer. Add an @ExceptionHandler for it in SpokeErrorHandler");
        }
    }

    private static void assertProblemJson(MvcResult r) {
        String type = r.getResponse().getContentType();
        assertNotNull(type, "the answer has no Content-Type. Return a ProblemDetail from your handler");
        assertTrue(MediaType.parseMediaType(type).isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                "the Content-Type should be application/problem+json, but it was " + type + ". Return a ProblemDetail from your handler");
    }

    // Reads one value from the JSON body, with a clear failure when the body is empty or the value is missing.
    private static <T> T json(MvcResult r, String path) throws Exception {
        String body = r.getResponse().getContentAsString();
        assertFalse(body.isBlank(), "the answer had an empty body, but it should carry JSON");
        try {
            return JsonPath.read(body, path);
        } catch (InvalidPathException e) {
            return fail("the answer " + body + " has no value at " + path);
        }
    }
}
