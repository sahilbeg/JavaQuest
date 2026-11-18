package jq.m10.launch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

// Stage 5: rentals live in H2 through JdbcRentalRepository, and the database itself guards "one active rental per bike".
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:launch-stage5;DB_CLOSE_DELAY=-1",
        "spring.main.banner-mode=off"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class Stage5PersistenceTest extends LaunchChecks {

    static final Instant START = Instant.parse("2026-11-18T09:00:00Z");
    static final Instant END = Instant.parse("2026-11-18T09:12:40Z");

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    RentalRepository wired;

    JdbcRentalRepository repo;

    @BeforeEach
    void createSchema() {
        repo = new JdbcRentalRepository(jdbc);
    }

    /** A row written straight into the table, as another Spoke server would. */
    private void insertActiveRow(String id, String bikeId) {
        jdbc.update("INSERT INTO rentals (id, city_id, rider_id, bike_id, bike_type, start_station_id, started_at, "
                        + "fare_cents, active_bike_id) VALUES (?, 'bramford', ?, ?, 'EBIKE', 'bramford-central', ?, 0, ?)",
                id, "rider-" + id, bikeId, OffsetDateTime.parse("2026-11-18T08:55:00Z"), bikeId);
    }

    private List<Map<String, Object>> row(String id) {
        return jdbc.queryForList("SELECT rider_id, bike_id, active_bike_id, ended_at, fare_cents FROM rentals WHERE id = ?",
                id);
    }

    @Test
    @DisplayName("Stage 5: LaunchWiring plugs JdbcRentalRepository into the core instead of the in-memory map")
    void wiringUsesJdbc() {
        assertTrue(wired instanceof JdbcRentalRepository,
                "The RentalRepository bean should be a JdbcRentalRepository, but was "
                        + wired.getClass().getSimpleName() + ". Change one line in LaunchWiring.rentalRepository.");
    }

    @Test
    @DisplayName("Stage 5: a rental started over HTTP is a row in the rentals table, and ending it clears active_bike_id")
    void apiWritesRows() throws Exception {
        String id = startedId("rider-51", "BR-2001", "castle-hill");
        List<Map<String, Object>> started = row(id);
        assertEquals(1, started.size(), "The rentals table should have one row with id " + id);
        assertEquals("rider-51", started.getFirst().get("RIDER_ID"), "rider_id should be stored");
        assertEquals("BR-2001", started.getFirst().get("ACTIVE_BIKE_ID"),
                "While the ride is on, active_bike_id should hold the bike id");

        clock.advance(Duration.ofMinutes(10));
        assertEquals(200, endRental(id, "university").getResponse().getStatus(), "Ending the ride should answer 200");

        Map<String, Object> ended = row(id).getFirst();
        assertNull(ended.get("ACTIVE_BIKE_ID"), "After the ride ends, active_bike_id should be NULL");
        assertNotNull(ended.get("ENDED_AT"), "After the ride ends, ended_at should be set");
        assertEquals(270L, ((Number) ended.get("FARE_CENTS")).longValue(), "CLASSIC, 10 min: 120 + 15 x 10 = 270");
    }

    @Test
    @DisplayName("Stage 5: save then findById gives back an equal Rental, active and ended")
    void roundTrip() {
        Rental active = Rental.start("rental-901", "bramford", "rider-901", "BR-7001", BikeType.EBIKE, "castle-hill",
                START, "k-901");
        repo.save(active);
        assertEquals(Optional.of(active), repo.findById("rental-901"),
                "findById should return a Rental equal to the one saved, every field included");

        Rental ended = active.end("university", END, 540);
        repo.save(ended);
        assertEquals(Optional.of(ended), repo.findById("rental-901"),
                "Saving the ended copy should update the row, and findById should return it");
        assertEquals(Optional.empty(), repo.findById("rental-999"), "An unknown id should give Optional.empty()");
    }

    @Test
    @DisplayName("Stage 5: findActiveByRider and findByIdempotencyKey find the right rows")
    void finders() {
        repo.save(Rental.start("rental-902", "bramford", "rider-902", "BR-7002", BikeType.CLASSIC, "castle-hill",
                START, "k-902"));
        repo.save(Rental.start("rental-903", "bramford", "rider-902", "BR-7003", BikeType.CLASSIC, "castle-hill",
                START, null).end("castle-hill", END, 300));

        assertEquals("rental-902", repo.findActiveByRider("rider-902").map(Rental::id).orElse(null),
                "rider-902's active rental is rental-902. rental-903 has ended.");
        assertEquals(Optional.empty(), repo.findActiveByRider("rider-904"), "rider-904 has no rental at all");
        assertEquals("rental-902", repo.findByIdempotencyKey("rider-902", "k-902").map(Rental::id).orElse(null),
                "rider-902 used key k-902 for rental-902");
        assertEquals(Optional.empty(), repo.findByIdempotencyKey("rider-904", "k-902"),
                "Keys belong to one rider: rider-904 never used k-902");
    }

    @Test
    @DisplayName("Stage 5: the database itself refuses a second active row for one bike, even from plain SQL")
    void databaseGuardsTheBike() {
        insertActiveRow("raw-1", "BR-7010");

        assertThrows(DataIntegrityViolationException.class, () -> insertActiveRow("raw-2", "BR-7010"),
                "A second active row for BR-7010 must break a UNIQUE constraint on active_bike_id. "
                        + "Java checks cannot stop another server; the database can.");
    }

    @Test
    @DisplayName("Stage 5: a bike may have many ended rentals but only one active one")
    void endedRowsDoNotBlock() {
        Rental first = Rental.start("rental-904", "bramford", "rider-905", "BR-7011", BikeType.EBIKE, "castle-hill",
                START, null);
        repo.save(first);
        assertThrows(DataIntegrityViolationException.class,
                () -> repo.save(Rental.start("rental-905", "bramford", "rider-906", "BR-7011", BikeType.EBIKE,
                        "castle-hill", START, null)),
                "Saving a second ACTIVE rental of BR-7011 should fail in the database");

        repo.save(first.end("canal-street", END, 540));
        assertDoesNotThrow(() -> repo.save(Rental.start("rental-906", "bramford", "rider-906", "BR-7011",
                        BikeType.EBIKE, "canal-street", END, null)),
                "Once the first ride ended (active_bike_id NULL), a new active rental of BR-7011 is fine");
    }

    @Test
    @DisplayName("Stage 5: when another server already rented the bike, POST /rentals answers 409 conflict, not 500")
    void conflictFromTheDatabase() throws Exception {
        insertActiveRow("raw-3", "BR-2044");

        MvcResult r = startRental("rider-52", "BR-2044", "bramford-central");

        assertEquals(409, r.getResponse().getStatus(), "The database already holds an active rental of BR-2044. "
                + "With JdbcRentalRepository wired in and the UNIQUE constraint on active_bike_id, the insert fails "
                + "and POST /rentals should answer 409");
        assertProblem(r, 409, "conflict", "Conflict");
        assertEquals(2, number(getJson("/stations/bramford-central/availability"), "$.ebikesAvailable"),
                "The refused request must leave nothing half-done: BR-2044 is still docked in this server's fleet");
    }
}
