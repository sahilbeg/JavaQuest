package jq.m03.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthlyReportSilverTest {

    @Test
    @DisplayName("Silver: averages keep their decimals")
    void exactAverages() {
        var averages = MonthlyReport.averageMinutesPerStation(List.of(
                new Ride("B-1042", "Harbor", "ebike", 20),
                new Ride("B-2001", "Harbor", "classic", 25),
                new Ride("B-3001", "Uptown", "cargo", 60)));
        assertEquals(22.5, averages.get("Harbor"), 0.001, "Harbor: (20 + 25) / 2 = 22.5");
        assertEquals(60.0, averages.get("Uptown"), 0.001, "Uptown: one ride of 60 minutes");
    }
}
