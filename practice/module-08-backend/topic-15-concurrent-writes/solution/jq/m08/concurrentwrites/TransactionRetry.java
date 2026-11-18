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
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, but was " + maxAttempts);
        }
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            for (int attempt = 1; ; attempt++) {
                try {
                    T result = work.run(connection);
                    connection.commit();
                    return result;
                } catch (SQLException e) {
                    connection.rollback();
                    if (!isRetryable(e) || attempt >= maxAttempts) {
                        throw e;
                    }
                } catch (RuntimeException e) {
                    connection.rollback();
                    throw e;
                }
            }
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }
}
