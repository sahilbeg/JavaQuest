package jq.m08.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Starts the API with MockMvc and fills the database before every check.
 *
 * <pre>
 * station    available            maintenance   docked  docks free
 * Riverside  2 CLASSIC, 1 EBIKE,  1 EBIKE       5       7
 *            1 CARGO
 * Old Mill   2 EBIKE              1 CLASSIC     3       9
 * Harbor     3 CLASSIC            -             3       9
 * Uptown     -                    1 CARGO       1       11
 * Depot      -                    -             0       12
 * Rented, so on the street with no station: B-1013 EBIKE, B-1014 CLASSIC, B-1015 EBIKE.
 * </pre>
 */
@SpringBootTest(properties = {"spring.jpa.open-in-view=false", "spring.main.banner-mode=off"})
@AutoConfigureMockMvc
abstract class StationApiTestBase {

    static final String PROBLEM_JSON = "application/problem+json";

    @Autowired
    MockMvc mvc;

    @Autowired
    StationRepository stations;

    @Autowired
    BikeRepository bikes;

    @BeforeEach
    void seed() {
        bikes.deleteAll();
        stations.deleteAll();
        // Saved out of alphabetical order on purpose, so the database does not hand them back sorted.
        Station riverside = stations.save(new Station("riverside", "Riverside", 12));
        Station oldMill = stations.save(new Station("old-mill", "Old Mill", 12));
        Station harbor = stations.save(new Station("harbor", "Harbor", 12));
        Station uptown = stations.save(new Station("uptown", "Uptown", 12));
        stations.save(new Station("depot", "Depot", 12));

        bikes.save(new Bike("B-1001", BikeType.CLASSIC, BikeStatus.AVAILABLE, riverside));
        bikes.save(new Bike("B-1002", BikeType.CLASSIC, BikeStatus.AVAILABLE, riverside));
        bikes.save(new Bike("B-1003", BikeType.EBIKE, BikeStatus.AVAILABLE, riverside));
        bikes.save(new Bike("B-1004", BikeType.EBIKE, BikeStatus.MAINTENANCE, riverside));
        bikes.save(new Bike("B-1005", BikeType.CARGO, BikeStatus.AVAILABLE, riverside));
        bikes.save(new Bike("B-1006", BikeType.EBIKE, BikeStatus.AVAILABLE, oldMill));
        bikes.save(new Bike("B-1007", BikeType.EBIKE, BikeStatus.AVAILABLE, oldMill));
        bikes.save(new Bike("B-1008", BikeType.CLASSIC, BikeStatus.MAINTENANCE, oldMill));
        bikes.save(new Bike("B-1009", BikeType.CLASSIC, BikeStatus.AVAILABLE, harbor));
        bikes.save(new Bike("B-1010", BikeType.CLASSIC, BikeStatus.AVAILABLE, harbor));
        bikes.save(new Bike("B-1011", BikeType.CLASSIC, BikeStatus.AVAILABLE, harbor));
        bikes.save(new Bike("B-1012", BikeType.CARGO, BikeStatus.MAINTENANCE, uptown));
        bikes.save(new Bike("B-1013", BikeType.EBIKE, BikeStatus.RENTED, null));
        bikes.save(new Bike("B-1014", BikeType.CLASSIC, BikeStatus.RENTED, null));
        bikes.save(new Bike("B-1015", BikeType.EBIKE, BikeStatus.RENTED, null));
    }

    MvcResult call(String url) throws Exception {
        return mvc.perform(get(url)).andReturn();
    }

    /** Calls url and checks it answers 200 with JSON, before anything reads the body. */
    MvcResult ok(String url) throws Exception {
        MvcResult r = call(url);
        assertEquals(200, r.getResponse().getStatus(), "GET " + url + " should answer 200 OK");
        assertEquals("application/json", r.getResponse().getContentType(), "GET " + url + " should answer with JSON");
        return r;
    }

    Object json(MvcResult r, String path) throws Exception {
        return JsonPath.read(r.getResponse().getContentAsString(), path);
    }

    List<String> names(MvcResult r) throws Exception {
        return JsonPath.read(r.getResponse().getContentAsString(), "$[*].name");
    }

    /** Checks the shared parts of a 400 answer, then that the problem names the bad parameter. */
    void assertInvalid(String url, String parameter) throws Exception {
        MvcResult r = call(url);
        assertEquals(400, r.getResponse().getStatus(), "GET " + url + " should answer 400 Bad Request");
        assertEquals(PROBLEM_JSON, r.getResponse().getContentType(),
                "GET " + url + " should answer with a ProblemDetail, whose Content-Type is application/problem+json");
        assertEquals("https://api.spoke.example/problems/invalid-request", json(r, "$.type"),
                "GET " + url + " should have the invalid-request problem type");
        assertEquals("Invalid request", json(r, "$.title"), "GET " + url + " should have the title \"Invalid request\"");
        assertEquals(400, json(r, "$.status"), "GET " + url + " should repeat the status 400 in the body");
        assertEquals(parameter, json(r, "$.parameter"),
                "GET " + url + " should name the bad query parameter in the extra property \"parameter\"");
    }
}
