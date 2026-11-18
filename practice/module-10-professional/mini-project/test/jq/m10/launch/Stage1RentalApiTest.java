package jq.m10.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MvcResult;

// Stage 1: the rentals API on top of the core. POST /rentals, GET /rentals/{id}, POST /rentals/{id}/end.
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:launch-stage1;DB_CLOSE_DELAY=-1",
        "spring.main.banner-mode=off"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class Stage1RentalApiTest extends LaunchChecks {

    static final Set<String> RENTAL_FIELDS = Set.of("id", "cityId", "riderId", "bikeId", "bikeType", "startStationId",
            "startedAt", "endStationId", "endedAt", "fareCents", "status");

    @Autowired
    RecordingEventPublisher events;

    @Test
    @DisplayName("Stage 1: POST /rentals answers 201 Created, a Location header and the new rental as JSON")
    void startAnswers201() throws Exception {
        MvcResult r = startRental("rider-1", "BR-2003", "castle-hill");

        assertEquals(201, r.getResponse().getStatus(), "POST /rentals should answer 201 Created");
        Object id = json(r, "$.id");
        assertTrue(id instanceof String text && text.matches("rental-\\d+"),
                "The JSON should hold the new rental's id, like rental-1, but was " + id);
        assertEquals("/rentals/" + id, r.getResponse().getHeader("Location"),
                "The Location header should point at the new rental: /rentals/" + id);
        assertEquals("bramford", json(r, "$.cityId"), "cityId should be bramford");
        assertEquals("rider-1", json(r, "$.riderId"), "riderId should come from the request");
        assertEquals("BR-2003", json(r, "$.bikeId"), "bikeId should come from the request");
        assertEquals("EBIKE", json(r, "$.bikeType"), "BR-2003 is an EBIKE");
        assertEquals("castle-hill", json(r, "$.startStationId"), "startStationId should come from the request");
        assertEquals("2026-11-18T09:00:00Z", json(r, "$.startedAt"), "startedAt should be the clock's time, in UTC");
        assertEquals("ACTIVE", json(r, "$.status"), "A new rental is ACTIVE");
        assertEquals(0, number(r, "$.fareCents"), "An active rental has fareCents 0");
    }

    @Test
    @DisplayName("Stage 1: the rental JSON has exactly the documented fields, and never the idempotency key")
    void exactFields() throws Exception {
        MvcResult r = startRental("rider-2", "BR-2004", "castle-hill");

        assertEquals(201, r.getResponse().getStatus(), "POST /rentals should answer 201 Created");
        assertEquals(RENTAL_FIELDS, fields(r), "Return RentalResponse.from(rental): the JSON should have exactly "
                + RENTAL_FIELDS + ", but had " + fields(r));
    }

    @Test
    @DisplayName("Stage 1: GET on the Location answers 200 with the same rental")
    void getReturnsTheRental() throws Exception {
        MvcResult created = startRental("rider-3", "BR-2001", "castle-hill");
        String location = created.getResponse().getHeader("Location");
        assertTrue(location != null && location.startsWith("/rentals/"),
                "POST /rentals should send a Location header like /rentals/rental-1, but sent " + location);

        MvcResult r = getJson(location);

        assertEquals(200, r.getResponse().getStatus(), "GET " + location + " should answer 200");
        assertEquals(created.getResponse().getContentAsString(), r.getResponse().getContentAsString(),
                "GET should return the same JSON that POST returned");
    }

    @Test
    @DisplayName("Stage 1: ending a Bramford EBIKE ride after 12 min 40 s answers 200, ENDED, 540 cents")
    void endChargesTheFare() throws Exception {
        String id = startedId("rider-4", "BR-2013", "canal-street");
        clock.advance(Duration.ofMinutes(12).plusSeconds(40));

        MvcResult r = endRental(id, "castle-hill");

        assertEquals(200, r.getResponse().getStatus(), "POST /rentals/{id}/end should answer 200");
        assertEquals("ENDED", json(r, "$.status"), "An ended rental has status ENDED");
        assertEquals(540, number(r, "$.fareCents"), "12 whole minutes on a Bramford EBIKE: 120 + 35 x 12 = 540");
        assertEquals("castle-hill", json(r, "$.endStationId"), "endStationId comes from the request body");
        assertEquals("2026-11-18T09:12:40Z", json(r, "$.endedAt"), "endedAt should be the clock's time");
        assertEquals(r.getResponse().getContentAsString(), getJson("/rentals/" + id).getResponse().getContentAsString(),
                "GET after the end should show the same ENDED rental");
    }

    @Test
    @DisplayName("Stage 1: after a ride ends, the bike can be rented again from where it was docked")
    void endedBikeCanBeRentedAgain() throws Exception {
        String id = startedId("rider-5", "BR-2014", "canal-street");
        clock.advance(Duration.ofMinutes(10));
        assertEquals(200, endRental(id, "university").getResponse().getStatus(), "Ending the ride should answer 200");

        MvcResult again = startRental("rider-6", "BR-2014", "university");

        assertEquals(201, again.getResponse().getStatus(),
                "BR-2014 was docked at university, so rider-6 should be able to rent it there");
        RideEnded event = events.published().stream().filter(e -> e.rentalId().equals(id)).findFirst().orElse(null);
        assertTrue(event != null, "The core should have published RideEnded for " + id);
        assertEquals(470, event.fareCents(), "EBIKE, 10 min in Bramford: 120 + 35 x 10 = 470");
        assertEquals(LocalDate.parse("2026-11-18"), event.serviceDate(), "The ride started on 18 November");
        assertNull(json(again, "$.endedAt"), "The new rental is active, so endedAt is null");
    }
}
