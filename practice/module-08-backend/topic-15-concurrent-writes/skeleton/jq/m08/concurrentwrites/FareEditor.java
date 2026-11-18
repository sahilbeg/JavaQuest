package jq.m08.concurrentwrites;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Lets ops staff change per-minute prices without silently overwriting each other (optimistic locking). */
public final class FareEditor {

    private FareEditor() {}

    /** Reads one fare with its version, or Optional.empty() when there is no such bike type. */
    public static Optional<Fare> read(Connection connection, String bikeType) throws SQLException {
        // TODO 2: SELECT bike_type, cents_per_minute, version FROM fare WHERE bike_type = ?
        //         and return Optional.of(new Fare(...)), or Optional.empty() when there is no row
        return Optional.empty();
    }

    /**
     * Sets a new price only if the row still has the version the editor read. Bumps the version by 1.
     * Returns false when someone else changed the fare first (or the type does not exist).
     */
    public static boolean updateWithVersion(Connection connection, String bikeType, long newCentsPerMinute,
                                            long expectedVersion) throws SQLException {
        // TODO 3: UPDATE fare SET cents_per_minute = ?, version = version + 1 WHERE bike_type = ? AND version = ?
        //         and return true only when exactly 1 row changed
        return false;
    }
}
