package jq.m12.feature;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * One statement month as a half-open range of instants: start is included, end is not.
 * Both are in UTC, and both are midnight in Riverton, which is where Spoke's statements are dated.
 */
public record MonthRange(YearMonth month, OffsetDateTime start, OffsetDateTime end) {

    /** Riverton's time zone. Statements for every city use it. */
    public static final ZoneId RIVERTON = ZoneId.of("America/New_York");

    public static MonthRange of(YearMonth month) {
        // TODO 1 (stage 1): these are midnight in UTC, but a Spoke month starts at midnight in Riverton.
        //   Use month.atDay(1).atStartOfDay(RIVERTON), then .withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime().
        //   Do the same for the first day of the next month.
        OffsetDateTime start = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = month.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return new MonthRange(month, start, end);
    }
}
