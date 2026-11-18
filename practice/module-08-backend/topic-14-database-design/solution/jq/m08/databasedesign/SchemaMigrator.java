package jq.m08.databasedesign;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

/** A tiny Flyway: applies each migration once, in version order, and remembers what it applied in schema_history. */
public final class SchemaMigrator {

    private SchemaMigrator() {}

    /** Applies every migration that is not in schema_history yet, lowest version first. Returns how many it applied. */
    public static int migrate(Connection connection, List<Migration> migrations) {
        List<Migration> ordered = inVersionOrder(migrations);
        try {
            createHistoryTable(connection);
            Map<Integer, Long> applied = appliedChecksums(connection);
            checkNothingChanged(ordered, applied);
            int count = 0;
            for (Migration migration : ordered) {
                if (!applied.containsKey(migration.version())) {
                    apply(connection, migration);
                    count++;
                }
            }
            return count;
        } catch (SQLException e) {
            throw new MigrationException("Could not read schema_history", e);
        }
    }

    /** A fingerprint of the SQL text. One changed character gives a different number. Given to you. */
    public static long checksum(String sql) {
        CRC32 crc = new CRC32();
        crc.update(sql.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }

    /** Creates the history table the first time. Given to you. */
    static void createHistoryTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS schema_history (
                        version     INT PRIMARY KEY,
                        description VARCHAR(200) NOT NULL,
                        checksum    BIGINT NOT NULL
                    )""");
        }
    }

    static List<Migration> inVersionOrder(List<Migration> migrations) {
        Set<Integer> seen = new HashSet<>();
        for (Migration migration : migrations) {
            if (!seen.add(migration.version())) {
                throw new IllegalArgumentException("Two migrations have version " + migration.version());
            }
        }
        List<Migration> ordered = new ArrayList<>(migrations);
        ordered.sort(Comparator.comparingInt(Migration::version));
        return ordered;
    }

    static Map<Integer, Long> appliedChecksums(Connection connection) throws SQLException {
        Map<Integer, Long> applied = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT version, checksum FROM schema_history")) {
            while (rows.next()) {
                applied.put(rows.getInt("version"), rows.getLong("checksum"));
            }
        }
        return applied;
    }

    static void checkNothingChanged(List<Migration> ordered, Map<Integer, Long> applied) {
        Map<Integer, Migration> byVersion = new HashMap<>();
        for (Migration migration : ordered) {
            byVersion.put(migration.version(), migration);
        }
        for (Map.Entry<Integer, Long> entry : applied.entrySet()) {
            Migration migration = byVersion.get(entry.getKey());
            if (migration == null) {
                throw new MigrationException("V" + entry.getKey() + " was applied, but it is missing from the list");
            }
            if (checksum(migration.sql()) != entry.getValue()) {
                throw new MigrationException("V" + entry.getKey() + " was changed after it was applied. "
                        + "Never edit an applied migration: add a new one instead.");
            }
        }
    }

    static void apply(Connection connection, Migration migration) {
        try {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement();
                 PreparedStatement record = connection.prepareStatement(
                         "INSERT INTO schema_history (version, description, checksum) VALUES (?, ?, ?)")) {
                statement.execute(migration.sql());
                record.setInt(1, migration.version());
                record.setString(2, migration.description());
                record.setLong(3, checksum(migration.sql()));
                record.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new MigrationException("V" + migration.version() + " failed: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new MigrationException("V" + migration.version() + " failed: " + e.getMessage(), e);
        }
    }
}
