package jq.m08.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcRideRepositoryTest {

    private JdbcDataSource h2;
    private TestDataSources.Counting counting;
    private JdbcRideRepository repository;

    @BeforeEach
    void freshDatabase() {
        h2 = TestDataSources.freshH2();
        JdbcRideRepository.createSchema(h2);
        counting = new TestDataSources.Counting(h2);
        repository = new JdbcRideRepository(counting.dataSource);
    }

    @AfterEach
    void dropDatabase() throws SQLException {
        TestDataSources.shutdown(h2);
    }

    @Test
    @DisplayName("save stores a ride and returns it with the id the database generated")
    void saveReturnsGeneratedId() {
        Ride saved = repository.save(new NewRide("r-17", "B-1042", 12, 460));

        assertTrue(saved.id() > 0, "save should return the id from getGeneratedKeys(), and the first id is 1, but it was " + saved.id());
        assertEquals(new Ride(saved.id(), "r-17", "B-1042", 12, 460), saved,
                "save should return a Ride with the generated id and the same rider, bike, minutes and fare");
    }

    @Test
    @DisplayName("Each saved ride gets a new, bigger id")
    void eachSaveGetsNewId() {
        Ride first = repository.save(new NewRide("r-17", "B-1042", 12, 460));
        Ride second = repository.save(new NewRide("r-23", "B-1088", 20, 400));

        assertTrue(second.id() > first.id(),
                "the second ride should get a bigger id than the first, but the ids were " + first.id() + " and " + second.id());
    }

    @Test
    @DisplayName("findById returns a saved ride, and empty for an id that does not exist")
    void findById() {
        Ride saved = repository.save(new NewRide("r-17", "B-1042", 12, 460));

        assertEquals(Optional.of(saved), repository.findById(saved.id()),
                "findById(" + saved.id() + ") should read back the ride that save returned, with every field the same");
        assertEquals(Optional.empty(), repository.findById(saved.id() + 1000),
                "findById should return Optional.empty() when no ride has that id");
    }

    @Test
    @DisplayName("findByRider returns only that rider's rides, ordered by id")
    void findByRider() {
        Ride first = repository.save(new NewRide("r-17", "B-1042", 12, 460));
        repository.save(new NewRide("r-23", "B-1088", 20, 400));
        Ride third = repository.save(new NewRide("r-17", "B-1076", 8, 340));

        assertEquals(List.of(first, third), repository.findByRider("r-17"),
                "findByRider(\"r-17\") should return r-17's two rides, oldest id first, and not r-23's ride");
        assertEquals(List.of(), repository.findByRider("r-99"),
                "findByRider should return an empty list for a rider with no rides");
    }

    @Test
    @DisplayName("findByRider treats injection text as a plain rider id, so it finds nobody")
    void findByRiderIsSafeFromInjection() {
        repository.save(new NewRide("r-1", "B-1042", 12, 460));
        repository.save(new NewRide("r-1", "B-1076", 8, 340));
        repository.save(new NewRide("r-2", "B-1088", 20, 400));

        assertEquals(2, repository.findByRider("r-1").size(), "findByRider(\"r-1\") should find r-1's 2 rides");
        assertEquals(List.of(), repository.findByRider("r-1' OR '1'='1"),
                "the text r-1' OR '1'='1 is not a real rider id, so findByRider must return no rides. "
                        + "If it returned every ride, the input was glued into the SQL. Use a ? parameter instead.");
        assertEquals(0, repository.totalFareCents("r-1' OR '1'='1"),
                "totalFareCents with injection text should be 0. Use a ? parameter, never string +.");
    }

    @Test
    @DisplayName("totalFareCents adds up one rider's fares, and is 0 for a rider with no rides")
    void totalFareCents() {
        repository.save(new NewRide("r-17", "B-1042", 12, 460));
        repository.save(new NewRide("r-23", "B-1088", 20, 400));
        repository.save(new NewRide("r-17", "B-1076", 8, 340));

        assertEquals(800, repository.totalFareCents("r-17"), "r-17's rides cost 460 + 340, so the total should be 800 cents");
        assertEquals(0, repository.totalFareCents("r-99"), "a rider with no rides should have a total of 0 cents");
    }

    @Test
    @DisplayName("deleteById removes one ride and returns true, then false when the ride is already gone")
    void deleteById() {
        Ride keep = repository.save(new NewRide("r-17", "B-1042", 12, 460));
        Ride remove = repository.save(new NewRide("r-17", "B-1076", 8, 340));

        assertTrue(repository.deleteById(remove.id()), "deleteById should return true when it deleted a ride");
        assertEquals(List.of(keep), repository.findByRider("r-17"), "after the delete, only the other ride should be left");
        assertFalse(repository.deleteById(remove.id()), "deleting the same id again should return false, because no row changed");
    }

    @Test
    @DisplayName("Every method closes the connections it opens")
    void closesEveryConnection() {
        Ride saved = repository.save(new NewRide("r-17", "B-1042", 12, 460));
        repository.findById(saved.id());
        repository.findById(saved.id() + 1000);
        repository.findByRider("r-17");
        repository.totalFareCents("r-17");
        repository.deleteById(saved.id());

        int opened = counting.opened.get();
        int closed = counting.closed.get();
        assertTrue(opened > 0, "the repository should get its connections from the DataSource, but it opened none");
        assertEquals(opened, closed, "every connection the repository opens should be closed again. It opened " + opened
                + " and closed " + closed + ". Open each Connection in a try-with-resources.");
    }

    @Test
    @DisplayName("A database error becomes a RideRepositoryException that keeps the SQLException as its cause")
    void wrapsSqlException() {
        JdbcRideRepository offline = new JdbcRideRepository(TestDataSources.broken());

        RideRepositoryException error = assertThrows(RideRepositoryException.class, () -> offline.findById(42),
                "when the database is down, findById should catch the SQLException and throw a RideRepositoryException");
        assertInstanceOf(SQLException.class, error.getCause(),
                "the RideRepositoryException should keep the SQLException as its cause: new RideRepositoryException(\"...\", e)");
    }
}
