package jq.m03.datetime;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RideTimes {

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        // TODO 1: return the whole minutes from start to end with Duration.between(start, end).toMinutes()
        return 0;
    }

    public static LocalDate passExpiry(LocalDate bought, int months) {
        // TODO 2: return the date that is the given number of months after bought (use plusMonths)
        return null;
    }

    public static boolean isWeekend(LocalDate day) {
        // TODO 3: return true when day.getDayOfWeek() is DayOfWeek.SATURDAY or DayOfWeek.SUNDAY
        return false;
    }

    public static String label(LocalDateTime start) {
        // TODO 4: format start with DateTimeFormatter.ofPattern("EEE d MMM HH:mm", Locale.ENGLISH)
        return "";
    }
}
