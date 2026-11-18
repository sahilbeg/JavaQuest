package jq.m09.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideEndServiceTest {

    private Databases rentals;
    private final AtomicInteger next = new AtomicInteger(1001);
    private final Supplier<String> eventIds = () -> "evt-" + next.getAndIncrement();

    @BeforeEach
    void open() throws SQLException {
        rentals = Databases.rentals();
    }

    @AfterEach
    void close() throws SQLException {
        rentals.close();
    }

    @Test
    @DisplayName("Ending an active ride saves the rental as ENDED and one outbox row, both committed")
    void endsRideAndWritesOutbox() throws SQLException {
        Optional<RideEnded> event = new RideEndService(eventIds).endRide(rentals.app, "rental-17", 20);

        RideEnded expected = new RideEnded("evt-1001", "rental-17", "rider-7", "B-1042", BikeType.EBIKE, 20, 700);
        assertEquals(Optional.of(expected), event,
                "rental-17 is rider-7's EBIKE B-1042: 20 minutes cost 100 + 20 x 30 = 700 cents, with event id evt-1001");
        assertEquals("ENDED|20|700", rentals.rows("SELECT status, minutes, fare_cents FROM rental WHERE id = 'rental-17'"),
                "rental-17 should be committed as status ENDED, minutes 20, fare_cents 700. "
                        + "Did you call commit()? The checks look through a second connection");
        assertEquals("evt-1001|ride-events|rider-7|" + expected.toJson() + "|FALSE",
                rentals.rows("SELECT event_id, topic, message_key, payload, sent FROM outbox"),
                "the outbox should hold one committed row: event_id evt-1001, topic ride-events, "
                        + "message_key = the rider id, payload = event.toJson(), and sent still FALSE");
        assertTrue(rentals.app.getAutoCommit(), "endRide should put auto-commit back the way it was (on) when it is done");
    }

    @Test
    @DisplayName("The fare in the event is 100 cents to unlock plus the minutes at the bike type's price")
    void faresPerBikeType() throws SQLException {
        RideEndService service = new RideEndService(eventIds);

        assertEquals(Optional.of(250L), service.endRide(rentals.app, "rental-18", 10).map(RideEnded::fareCents),
                "rental-18 is an active CLASSIC ride: the event's fare should be 100 + 10 x 15 = 250 cents");
        assertEquals(Optional.of(200L), service.endRide(rentals.app, "rental-19", 4).map(RideEnded::fareCents),
                "rental-19 is an active CARGO ride: the event's fare should be 100 + 4 x 25 = 200 cents");
        assertEquals("rental-18|250, rental-19|200",
                rentals.rows("SELECT id, fare_cents FROM rental WHERE status = 'ENDED' AND id <> 'rental-12' ORDER BY id"),
                "both rentals should be saved as ENDED with their fares");
        assertEquals("evt-1001|rider-9, evt-1002|rider-12",
                rentals.rows("SELECT event_id, message_key FROM outbox ORDER BY id"),
                "each ended ride should add its own outbox row, keyed by its rider, in the order the rides ended");
    }

    @Test
    @DisplayName("An unknown or already-ended rental returns empty and writes nothing")
    void unknownOrEndedRental() throws SQLException {
        RideEndService service = new RideEndService(eventIds);

        assertEquals(Optional.empty(), service.endRide(rentals.app, "rental-99", 12),
                "there is no rental-99, so endRide should return Optional.empty()");
        assertEquals(Optional.empty(), service.endRide(rentals.app, "rental-12", 12),
                "rental-12 already ENDED, so ending it again should return Optional.empty()");
        assertEquals("ENDED|10|400", rentals.rows("SELECT status, minutes, fare_cents FROM rental WHERE id = 'rental-12'"),
                "rental-12 must keep its old minutes and fare");
        assertEquals("0", rentals.text("SELECT COUNT(*) FROM outbox"),
                "no ride ended, so no event may go into the outbox");
        assertTrue(rentals.app.getAutoCommit(), "endRide should put auto-commit back the way it was (on) when it is done");
    }

    @Test
    @DisplayName("When the outbox insert fails, the rental update is rolled back too: neither row is saved")
    void outboxFailureRollsBackRental() throws SQLException {
        Databases.addOutboxRow(rentals.observer, "evt-1001", "rider-4", true);
        RideEndService service = new RideEndService(() -> "evt-1001");   // an id the outbox already holds

        SQLException error = assertThrows(SQLException.class, () -> service.endRide(rentals.app, "rental-17", 20),
                "evt-1001 is already in the outbox, so the outbox INSERT fails. endRide should throw that SQLException");
        assertEquals("23505", error.getSQLState(), "the error should be the outbox's duplicate event_id (SQLState 23505)");

        assertEquals("ACTIVE|null|null", rentals.rows("SELECT status, minutes, fare_cents FROM rental WHERE id = 'rental-17'"),
                "the outbox row failed, so the rental update must be rolled back: rental-17 stays ACTIVE. "
                        + "Run both writes in ONE transaction: setAutoCommit(false), commit() at the end, rollback() on an error");
        assertEquals("1", rentals.text("SELECT COUNT(*) FROM outbox"), "only the old evt-1001 row may be in the outbox");
        assertTrue(rentals.app.getAutoCommit(), "endRide should put auto-commit back on after a failure too (use finally)");
    }
}
