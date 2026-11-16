package jq.m08.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class StationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ApplicationContext context;

    /** Sends GET path through MockMvc. No real server and no real port. */
    private MvcResult getPath(String path) throws Exception {
        return mvc.perform(get(path)).andReturn();
    }

    /** Checks the status, then returns the body as text. */
    private String bodyOf(String path) throws Exception {
        MvcResult result = getPath(path);
        int status = result.getResponse().getStatus();
        assertEquals(200, status, "GET " + path + " should answer 200 OK, but it answered " + status
                + ". A 404 means Spring found no method for this path: check @RestController on the class"
                + " and @GetMapping(\"" + path + "\") on the method");
        String body = result.getResponse().getContentAsString();
        assertFalse(body.isBlank(), "GET " + path + " answered with an empty body. Does the method return a value?");
        return body;
    }

    @Test
    @DisplayName("Spring creates a StationController bean when the app starts")
    void controllerIsABean() {
        assertEquals(1, context.getBeanNamesForType(StationController.class).length,
                "Spring has no StationController bean. Put @RestController above the class (TODO 1)");
    }

    @Test
    @DisplayName("GET /stations answers 200 OK")
    void stationsAnswersOk() throws Exception {
        bodyOf("/stations");
    }

    @Test
    @DisplayName("GET /stations sends JSON")
    void stationsSendsJson() throws Exception {
        bodyOf("/stations");
        String contentType = getPath("/stations").getResponse().getContentType();
        assertNotNull(contentType, "GET /stations should say what it sent with a Content-Type header, but it had none");
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(contentType)),
                "GET /stations should send Content-Type application/json, but it sent " + contentType);
    }

    @Test
    @DisplayName("GET /stations lists the five stations in order")
    void stationsListsAllFive() throws Exception {
        String body = bodyOf("/stations");
        List<String> names = JsonPath.read(body, "$[*]");
        assertEquals(5, names.size(), "GET /stations should list 5 stations, but the JSON was " + body
                + ". Return the STATIONS list (TODO 3)");
        assertEquals(List.of("Riverside", "Old Mill", "Harbor", "Uptown", "Depot"), names,
                "GET /stations should list the stations in this order: Riverside, Old Mill, Harbor, Uptown, Depot");
    }

    @Test
    @DisplayName("GET /health answers 200 OK with JSON")
    void healthAnswersOkWithJson() throws Exception {
        bodyOf("/health");
        String contentType = getPath("/health").getResponse().getContentType();
        assertNotNull(contentType, "GET /health should send a Content-Type header, but it had none");
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(contentType)),
                "GET /health should send Content-Type application/json, but it sent " + contentType);
    }

    @Test
    @DisplayName("GET /health says the status is UP")
    void healthSaysUp() throws Exception {
        String body = bodyOf("/health");
        String status = JsonPath.read(body, "$.status");
        assertEquals("UP", status, "GET /health should send \"status\": \"UP\", but the JSON was " + body
                + ". Return new Health(\"UP\", ...) (TODO 5)");
    }

    @Test
    @DisplayName("GET /health counts five stations")
    void healthCountsStations() throws Exception {
        String body = bodyOf("/health");
        int stations = JsonPath.read(body, "$.stations");
        assertEquals(5, stations, "GET /health should send \"stations\": 5, but the JSON was " + body
                + ". Use STATIONS.size() for the count (TODO 5)");
    }

    @Test
    @DisplayName("A path the API does not know answers 404 Not Found")
    void unknownPathIsNotFound() throws Exception {
        int status = getPath("/bikes").getResponse().getStatus();
        assertEquals(404, status, "GET /bikes has no method yet, so it should answer 404 Not Found, but it answered " + status);
    }
}
