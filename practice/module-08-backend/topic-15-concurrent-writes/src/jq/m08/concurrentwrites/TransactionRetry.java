package jq.m08.concurrentwrites;

import java.sql.Connection;
import java.sql.SQLException;

/** Runs work in a transaction, and runs the WHOLE transaction again when the database says "retry me". */
public final class TransactionRetry {

    private TransactionRetry() {}

    /** A serialization failure (40001, which H2 also uses for deadlocks) or a PostgreSQL deadlock (40P01). Given to you. */
    static boolean isRetryable(SQLException e) {
        return "40001".equals(e.getSQLState()) || "40P01".equals(e.getSQLState());
    }

    public static <T> T inTransaction(Connection connection, int maxAttempts, SqlWork<T> work) throws SQLException {
        // TODO 7: throw IllegalArgumentException when maxAttempts < 1. Remember getAutoCommit(), then setAutoCommit(false).
        // TODO 8: loop: run work, commit, return its result. On an SQLException, roll back; if isRetryable(e) and
        //         attempts are left, go round again from the start, otherwise throw e. On a RuntimeException, roll back and throw it.
        // TODO 9: in a finally block, put auto-commit back to what it was
        return work.run(connection);
    }
}
