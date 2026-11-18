package jq.m12.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthEndGoldTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final LocalDateTime FIRST = LocalDateTime.of(2026, 10, 1, 6, 0);

    private final ReportDatabase db = new ReportDatabase();
    private final CouncilExport export = new CouncilExport(db.dataSource);

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    /** Rides 1 to 20, one every hour from 06:00 on 1 October, plus one ride in September and one in November. */
    private void seedTwentyRides() {
        for (int id = 1; id <= 20; id++) {
            db.ride(id, "rider-" + id, "harbor", FIRST.plusHours(id - 1), 10, 250);
        }
        db.ride(901, "rider-1", "harbor", LocalDateTime.parse("2026-09-30T23:59:59"), 10, 250);
        db.ride(902, "rider-1", "harbor", LocalDateTime.parse("2026-11-01T00:00:00"), 10, 250);
    }

    @Test
    @DisplayName("Gold: on a quiet table, pages of 6 return October's 20 rides in order, and each cursor names the last row")
    void quietTablePagesInOrder() {
        seedTwentyRides();
        List<ExportPage> pages = new ArrayList<>();
        List<Long> ids = exportAll(6, pages, page -> { });
        assertEquals(range(1, 20), ids, "October's rides are 1 to 20, in started_at order, each exactly once. "
                + "Rides 901 (September) and 902 (November) are not in October");
        assertEquals(4, pages.size(), "20 rides in pages of 6 make 4 pages: 6, 6, 6 and 2");
        ExportRow last = pages.get(0).rows().get(5);
        assertEquals(last.startedAt() + "|" + last.id(), pages.get(0).nextCursor(),
                "nextCursor is the last row's startedAt (LocalDateTime.toString()), a '|', then its id: "
                        + "it says where the page ended, not how many rows to skip");
        assertNull(pages.get(3).nextCursor(), "the last page has fewer than 6 rows, so its nextCursor is null");
    }

    @Test
    @DisplayName("Gold: rides that finish during the export and started earlier do not make any ride repeat")
    void lateRidesDoNotRepeatRows() {
        seedTwentyRides();
        long[] nextId = {100};
        List<Long> ids = exportAll(5, new ArrayList<>(), page -> {
            // A ride that started at 06:30 on 1 October ends now, so its row arrives in the middle of the export.
            db.ride(nextId[0]++, "rider-99", "harbor", FIRST.plusMinutes(30), 45, 775);
        });
        assertNoDuplicates(ids);
        assertTrue(ids.containsAll(range(1, 20)), "every ride that existed when the export began must be in it, "
                + "but these were missing: " + missing(ids));
    }

    @Test
    @DisplayName("Gold: a ride deleted during the export does not make another ride go missing")
    void deletedRideDoesNotSkipRows() {
        seedTwentyRides();
        int[] pagesRead = {0};
        List<Long> ids = exportAll(5, new ArrayList<>(), page -> {
            if (pagesRead[0]++ == 0) {
                db.execute("DELETE FROM ride WHERE id = 2");   // support cancels a test ride from page 1
            }
        });
        assertNoDuplicates(ids);
        List<Long> expected = new ArrayList<>(range(1, 20));
        expected.remove(Long.valueOf(2));
        assertTrue(ids.containsAll(expected), "only ride 2 was deleted, yet these rides were skipped: "
                + missingFrom(ids, expected) + ". An OFFSET counts rows, so when an earlier row goes, every later row "
                + "moves up one place and one is jumped over. A cursor on (started_at, id) does not move");
    }

    @Test
    @DisplayName("Gold: rides that start in the same second are neither skipped nor repeated at a page boundary")
    void tiesOnStartedAtAreNotSkipped() {
        LocalDateTime rush = LocalDateTime.of(2026, 10, 7, 8, 0);
        for (int id = 1; id <= 7; id++) {
            db.ride(id, "rider-" + id, "riverside", rush, 10, 250);   // seven riders unlock at 08:00:00
        }
        List<Long> ids = exportAll(3, new ArrayList<>(), page -> { });
        assertEquals(range(1, 7), ids, "all 7 rides start at 08:00:00, so the order is by id: 1 to 7, each once, but the "
                + "export gave " + ids + ". "
                + "'started_at > cursor' alone skips the rest of a tie: use started_at > t OR (started_at = t AND id > id)");
    }

    @Test
    @DisplayName("Gold: the council's script can resume from a cursor it saved")
    void resumeFromASavedCursor() {
        seedTwentyRides();
        String saved = FIRST.plusHours(11) + "|12";   // "2026-10-01T17:00|12"
        ExportPage page = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try {
                return export.page(OCTOBER, saved, 4);
            } catch (RuntimeException e) {
                throw new AssertionError("page could not read the cursor " + saved + ": " + e
                        + ". A cursor is the last row's startedAt, a '|', then its id", e);
            }
        });
        assertEquals(List.of(13L, 14L, 15L, 16L), page.rows().stream().map(ExportRow::id).toList(),
                "the cursor " + saved + " means 'after ride 12, which started at 17:00', so the page is rides 13 to 16");
    }

    /** Reads every page, calling betweenPages after each page that has a next cursor. */
    private List<Long> exportAll(int pageSize, List<ExportPage> pages, Consumer<ExportPage> betweenPages) {
        return assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            List<Long> ids = new ArrayList<>();
            String cursor = null;
            do {
                ExportPage page = export.page(OCTOBER, cursor, pageSize);
                pages.add(page);
                page.rows().forEach(row -> ids.add(row.id()));
                cursor = page.nextCursor();
                if (cursor != null) {
                    betweenPages.accept(page);
                }
                assertTrue(pages.size() <= 50, "the export should end, but it has read 50 pages: does the last page's "
                        + "nextCursor become null when it has fewer rows than pageSize?");
            } while (cursor != null);
            return ids;
        }, "the export took more than 5 seconds: does each page move past the last one?");
    }

    private static void assertNoDuplicates(List<Long> ids) {
        Set<Long> seen = new HashSet<>();
        List<Long> repeated = ids.stream().filter(id -> !seen.add(id)).toList();
        assertTrue(repeated.isEmpty(), "the council got these rides twice: " + repeated + ". With LIMIT/OFFSET, a new row "
                + "that sorts before the offset pushes every later row back one place. Page by a cursor on (started_at, id)");
    }

    private static List<Long> missing(List<Long> ids) {
        return missingFrom(ids, range(1, 20));
    }

    private static List<Long> missingFrom(List<Long> ids, List<Long> expected) {
        return expected.stream().filter(id -> !ids.contains(id)).toList();
    }

    private static List<Long> range(long from, long to) {
        List<Long> ids = new ArrayList<>();
        for (long id = from; id <= to; id++) {
            ids.add(id);
        }
        return ids;
    }
}
