package jq.m03.datetime;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RideTimes {

    private static final DateTimeFormatter LABEL =
            DateTimeFormatter.ofPattern("EEE d MMM HH:mm", Locale.ENGLISH);

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    public static LocalDate passExpiry(LocalDate bought, int months) {
        return bought.plusMonths(months);
    }

    public static boolean isWeekend(LocalDate day) {
        DayOfWeek dow = day.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    public static String label(LocalDateTime start) {
        return start.format(LABEL);
    }
}
