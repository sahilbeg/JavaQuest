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
        // TODO 5: throw IllegalArgumentException if two migrations share a version,
        //         then return a NEW list sorted by version, lowest first (do not sort the list you were given)
        return migrations;
    }

    static Map<Integer, Long> appliedChecksums(Connection connection) throws SQLException {
        // TODO 6: SELECT version, checksum FROM schema_history and put each row in a map: version -> checksum
        return new HashMap<>();
    }

    static void checkNothingChanged(List<Migration> ordered, Map<Integer, Long> applied) {
        // TODO 7: for every applied version, find the migration with that version in ordered.
        //         Missing, or checksum(migration.sql()) differs from the stored checksum? Throw a MigrationException
        //         whose message starts with "V" and the version, for example "V2 was changed after it was applied"
    }

    static void apply(Connection connection, Migration migration) {
        // TODO 8: in ONE transaction, run migration.sql() and insert (version, description, checksum(sql)) into schema_history.
        //         Turn auto-commit off, commit at the end, roll back on SQLException and throw a MigrationException
        //         whose message starts with "V" and the version, with the SQLException as its cause. Put auto-commit back as it was.
    }
}
