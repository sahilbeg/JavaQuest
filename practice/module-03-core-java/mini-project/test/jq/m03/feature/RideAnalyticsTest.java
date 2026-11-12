package jq.m03.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RideAnalyticsTest {

    private static final String LOG = """
            bikeId,station,type,minutes
            B-1042,Harbor,ebike,40
            B-2001,Riverside,classic,30
            B-3001,Harbor,cargo,twenty
            B-4001,Old Mill,tandem,10

            B-5001,Harbor,ebike,25
            """;

    @TempDir
    Path dir;

    private ImportResult importSample() throws IOException {
        Path file = dir.resolve("harbor.csv");
        Files.writeString(file, LOG);
        return RideLogReader.read(file);
    }

    @Test
    @DisplayName("good lines become rides and bad lines become numbered errors")
    void importsAndCollectsErrors() throws IOException {
        ImportResult result = importSample();
        assertEquals(List.of("B-1042", "B-2001", "B-5001"), result.rides().stream().map(Ride::bikeId).toList(),
                "three lines are valid rides");
        assertEquals(List.of("line 4: bad minutes: twenty", "line 5: unknown type: tandem"), result.errors(),
                "line numbers count from 1 and the header is line 1; the blank line is skipped silently");
    }

    @Test
    @DisplayName("a line without 4 values is reported")
    void wrongNumberOfValues() throws IOException {
        Path file = dir.resolve("short.csv");
        Files.writeString(file, "bikeId,station,type,minutes\nB-1,Harbor\n");
        assertEquals(List.of("line 2: expected 4 values"), RideLogReader.read(file).errors(),
                "B-1,Harbor has only 2 values");
    }

    @Test
    @DisplayName("revenue, busiest station and minutes per type")
    void analytics() throws IOException {
        List<Ride> rides = importSample().rides();
        assertEquals(27.00, RideAnalytics.revenue(rides), 0.001, "13.00 + 5.50 + 8.50 = 27.00");
        assertEquals(Optional.of("Harbor"), RideAnalytics.busiestStation(rides), "Harbor has 65 minutes");
        assertEquals(Map.of("classic", 30, "ebike", 65), RideAnalytics.minutesPerType(rides), "minutes per type");
        assertEquals(Optional.empty(), RideAnalytics.busiestStation(List.of()), "no rides means no busiest station");
    }

    @Test
    @DisplayName("the report matches the KB page")
    void report() throws IOException {
        String expected = """
                Rides: 3 (2 bad lines)
                Revenue: $27.00
                Busiest station: Harbor
                classic: 30 min
                ebike: 65 min
                line 4: bad minutes: twenty
                line 5: unknown type: tandem""";
        assertEquals(expected, RideAnalytics.report(importSample()), "compare with the report in the KB page, line by line");
    }
}
