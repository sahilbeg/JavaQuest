package jq.m08.databasedesign;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Helpers for the tests: a fresh in-memory database each time, and small queries. */
final class TestDatabases {

    private static final AtomicInteger NEXT_NAME = new AtomicInteger();

    private TestDatabases() {}

    /** A brand-new in-memory database. It disappears when this connection is closed. */
    static Connection fresh() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:spoke-design-" + NEXT_NAME.incrementAndGet());
    }

    static void run(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    static boolean tableExists(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                     + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = '" + table.toUpperCase() + "'")) {
            rows.next();
            return rows.getInt(1) == 1;
        }
    }

    /** The versions in schema_history, lowest first, or an empty list when the table does not exist yet. */
    static List<Integer> historyVersions(Connection connection) throws SQLException {
        List<Integer> versions = new ArrayList<>();
        if (!tableExists(connection, "schema_history")) {
            return versions;
        }
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT version FROM schema_history ORDER BY version")) {
            while (rows.next()) {
                versions.add(rows.getInt(1));
            }
        }
        return versions;
    }

    static String text(Connection connection, String sql) throws SQLException {
        StringBuilder out = new StringBuilder();
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                out.append(rows.getString(1)).append('\n');
            }
        }
        return out.toString();
    }
}
