package jq.m08.concurrentwrites;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionRetryTest {

    private TwoServers servers;
    private Connection db;
    private final AtomicInteger calls = new AtomicInteger();

    @BeforeEach
    void payments() throws SQLException {
        servers = new TwoServers("CREATE TABLE payment (id VARCHAR(20) PRIMARY KEY, cents BIGINT NOT NULL)");
        db = servers.serverA;
    }

    @AfterEach
    void close() throws SQLException {
        servers.close();
    }

    private static void insert(Connection connection, String id) throws SQLException {
        TwoServers.run(connection, "INSERT INTO payment VALUES ('" + id + "', 460)");
    }

    /** What another connection sees: only committed rows. */
    private String committed() throws SQLException {
        return TwoServers.text(servers.serverB, "SELECT id FROM payment ORDER BY id");
    }

    private <T> T within2s(org.junit.jupiter.api.function.ThrowingSupplier<T> call) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), call, "inTransaction should stop. Does the loop ever end?");
    }

    @Test
    @DisplayName("The work runs with auto-commit off, is committed at the end, and auto-commit is put back")
    void commitsOnce() throws SQLException {
        String result = within2s(() -> TransactionRetry.inTransaction(db, 3, connection -> {
            calls.incrementAndGet();
            assertFalse(connection.getAutoCommit(), "the work must run inside a transaction: call setAutoCommit(false) first");
            insert(connection, "pay-1");
            insert(connection, "pay-2");
            return "paid";
        }));

        assertEquals("paid", result, "inTransaction should return what the work returned");
        assertEquals(1, calls.get(), "the work succeeded, so it should run exactly once");
        assertEquals("pay-1, pay-2", committed(), "both rows should be committed, so another connection can see them");
        assertTrue(db.getAutoCommit(), "auto-commit was on before, so it must be on again afterwards");
    }

    @Test
    @DisplayName("A deadlock is retried from the start, and only the successful attempt's rows are kept")
    void retriesADeadlock() throws SQLException {
        String result = assertDoesNotThrow(() -> within2s(() -> TransactionRetry.inTransaction(db, 3, connection -> {
            int attempt = calls.incrementAndGet();
            insert(connection, "pay-" + attempt);
            if (attempt < 3) {
                throw new SQLException("deadlock detected", "40P01");
            }
            return "paid on attempt " + attempt;
        })), "a deadlock (SQLState 40P01) should be rolled back and retried, not thrown to the caller");

        assertEquals(3, calls.get(), "two deadlocks then a success: the work should run 3 times");
        assertEquals("paid on attempt 3", result, "inTransaction should return the successful attempt's result");
        assertEquals("pay-3", committed(), "attempts 1 and 2 must be rolled back, so only pay-3 is left");
    }

    @Test
    @DisplayName("An error that is not a deadlock is rolled back and thrown at once, with no retry")
    void otherErrorsAreNotRetried() throws SQLException {
        insert(db, "pay-1");

        SQLException error = assertThrows(SQLException.class, () -> TransactionRetry.inTransaction(db, 3, connection -> {
            calls.incrementAndGet();
            insert(connection, "pay-2");
            insert(connection, "pay-1");   // duplicate id: a real constraint violation, retrying cannot fix it
            return "paid";
        }), "a duplicate id should be thrown to the caller");

        assertFalse(TransactionRetry.isRetryable(error), "the error thrown should be the constraint violation itself");
        assertEquals(1, calls.get(), "a constraint violation will fail every time, so the work must run only once");
        assertEquals("pay-1", committed(), "pay-2 must be rolled back, leaving only the old pay-1");
        assertTrue(db.getAutoCommit(), "auto-commit must be put back even when the work fails");
    }

    @Test
    @DisplayName("After maxAttempts deadlocks it gives up, rolls back and throws the last error")
    void givesUp() throws SQLException {
        SQLTransactionRollbackException deadlock = new SQLTransactionRollbackException("Deadlock detected", "40001");

        SQLException error = assertThrows(SQLException.class,
                () -> within2s(() -> TransactionRetry.inTransaction(db, 3, connection -> {
                    insert(connection, "pay-" + calls.incrementAndGet());
                    throw deadlock;
                })), "after 3 deadlocks, inTransaction should throw");

        assertSame(deadlock, error, "the caller should get the deadlock exception itself");
        assertEquals(3, calls.get(), "maxAttempts is 3, so the work should run exactly 3 times");
        assertEquals("", committed(), "every attempt must be rolled back, so no payment is saved");
        assertTrue(db.getAutoCommit(), "auto-commit must be put back after giving up");
    }
}
