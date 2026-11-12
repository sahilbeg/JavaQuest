package jq.m03.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideTimesTest {

    @Test
    @DisplayName("minutesBetween counts a short ride on the same day")
    void sameDay() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 14, 9, 15);
        LocalDateTime end = LocalDateTime.of(2026, 11, 14, 9, 40);
        assertEquals(25, RideTimes.minutesBetween(start, end),
                "A ride from 09:15 to 09:40 should last 25 minutes");
    }

    @Test
    @DisplayName("minutesBetween works for a ride that crosses midnight")
    void acrossMidnight() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 14, 23, 50);
        LocalDateTime end = LocalDateTime.of(2026, 11, 15, 0, 20);
        assertEquals(30, RideTimes.minutesBetween(start, end),
                "A ride from 23:50 to 00:20 the next day should last 30 minutes. Use Duration.between with the full date and time");
    }

    @Test
    @DisplayName("minutesBetween works for a bike kept for more than a day")
    void acrossDays() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 14, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 16, 10, 30);
        assertEquals(2910, RideTimes.minutesBetween(start, end),
                "Two days and 30 minutes is 2 * 1440 + 30 = 2910 minutes");
    }

    @Test
    @DisplayName("passExpiry adds whole months to the purchase date")
    void passExpiry() {
        assertEquals(LocalDate.of(2026, 12, 14), RideTimes.passExpiry(LocalDate.of(2026, 11, 14), 1),
                "A 1-month pass bought on 2026-11-14 should expire on 2026-12-14");
        assertEquals(LocalDate.of(2027, 2, 28), RideTimes.passExpiry(LocalDate.of(2027, 1, 31), 1),
                "A 1-month pass bought on 2027-01-31 should expire on 2027-02-28, the last day of February. plusMonths handles this for you");
        assertEquals(LocalDate.of(2027, 5, 14), RideTimes.passExpiry(LocalDate.of(2026, 11, 14), 6),
                "A 6-month pass bought on 2026-11-14 should expire on 2027-05-14");
    }

    @Test
    @DisplayName("isWeekend is true only on Saturday and Sunday")
    void weekend() {
        assertTrue(RideTimes.isWeekend(LocalDate.of(2026, 11, 14)), "2026-11-14 is a Saturday, so isWeekend should be true");
        assertTrue(RideTimes.isWeekend(LocalDate.of(2026, 11, 15)), "2026-11-15 is a Sunday, so isWeekend should be true");
        assertFalse(RideTimes.isWeekend(LocalDate.of(2026, 11, 13)), "2026-11-13 is a Friday, so isWeekend should be false");
        assertFalse(RideTimes.isWeekend(LocalDate.of(2026, 11, 16)), "2026-11-16 is a Monday, so isWeekend should be false");
    }

    @Test
    @DisplayName("label shows the day, date and time in English")
    void label() {
        assertEquals("Fri 13 Nov 23:50", RideTimes.label(LocalDateTime.of(2026, 11, 13, 23, 50)),
                "label should use the pattern \"EEE d MMM HH:mm\" with Locale.ENGLISH");
        assertEquals("Mon 2 Feb 07:05", RideTimes.label(LocalDateTime.of(2026, 2, 2, 7, 5)),
                "label should show the day number without a leading zero and the time with two digits: Mon 2 Feb 07:05");
    }
}
