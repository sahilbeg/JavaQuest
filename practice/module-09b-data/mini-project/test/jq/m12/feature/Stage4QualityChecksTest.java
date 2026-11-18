package jq.m12.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import jq.m12.feature.MartDatabase.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Stage4QualityChecksTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final MartDatabase db = new MartDatabase();
    private final StatementJob job = new StatementJob(db.dataSource);

    @BeforeEach
    void seed() {
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-10T12:00Z", 10);  // 250
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-11T12:00Z", 10);  // 250
        db.ride("rider-8", "riverton", "CARGO", "2026-10-12T12:00Z", 8);     // 300
    }

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    @Test
    @DisplayName("Stage 4: a negative fare fails no-negative-fares, and last night's rows stay")
    void negativeFareRollsBack() {
        job.runMonth(OCTOBER);
        List<Row> before = db.rows(OCTOBER);
        db.ride("rider-8", "riverton", "CLASSIC", "2026-10-20T12:00Z", 10, -250);   // a refund saved as a ride

        DataQualityException failure = assertThrows(DataQualityException.class, () -> job.runMonth(OCTOBER),
                "a ride with fare_cents -250 must stop the run with a DataQualityException");

        assertEquals("no-negative-fares", failure.check(), "the failed check is no-negative-fares");
        assertTrue(failure.getMessage().contains("no-negative-fares"),
                "the message should name the failed check, so the on-call engineer knows where to look: "
                        + failure.getMessage());
        assertFalse(before.isEmpty(), "the first run should have built October");
        assertEquals(before, db.rows(OCTOBER), "a failed check rolls the whole run back: October keeps last night's rows");
    }

    @Test
    @DisplayName("Stage 4: statement totals that differ from the raw rides fail totals-match")
    void totalsMustMatch() throws SQLException {
        db.execute("INSERT INTO rider_monthly_statement VALUES ('2026-10', 'riverton', 'rider-7', 2, 500, 10, 1, NULL)");
        db.execute("INSERT INTO rider_monthly_statement VALUES ('2026-10', 'riverton', 'rider-8', 1, 250, 8, 2, NULL)");

        DataQualityException failure = verifyFails();

        assertEquals("totals-match", failure.check(),
                "the statements add up to 750 cents, but October's raw rides add up to 800: totals-match must fail");
        assertTrue(failure.getMessage().contains("totals-match"), "the message should name the check: " + failure.getMessage());
    }

    @Test
    @DisplayName("Stage 4: a rider with rides but no statement row fails every-rider-has-a-row")
    void everyRiderNeedsARow() throws SQLException {
        // rider-7's row wrongly holds rider-8's money too, so the totals still match. Only rider-8's row is missing.
        db.execute("INSERT INTO rider_monthly_statement VALUES ('2026-10', 'riverton', 'rider-7', 3, 800, 10, 1, NULL)");

        DataQualityException failure = verifyFails();

        assertEquals("every-rider-has-a-row", failure.check(),
                "rider-8 rode in Riverton in October but has no row: every-rider-has-a-row must fail");
        assertTrue(failure.getMessage().contains("every-rider-has-a-row"),
                "the message should name the check: " + failure.getMessage());
    }

    @Test
    @DisplayName("Stage 4: a correct month passes all three checks")
    void correctMonthPasses() throws SQLException {
        job.runMonth(OCTOBER);
        assertEquals(2, db.rows(OCTOBER).size(), "runMonth should build rows for rider-7 and rider-8");
        try (Connection connection = db.dataSource.getConnection()) {
            QualityChecks.verify(connection, MonthRange.of(OCTOBER));
        }
    }

    private DataQualityException verifyFails() throws SQLException {
        try (Connection connection = db.dataSource.getConnection()) {
            return assertThrows(DataQualityException.class,
                    () -> QualityChecks.verify(connection, MonthRange.of(OCTOBER)),
                    "QualityChecks.verify should throw a DataQualityException for this month");
        }
    }
}
