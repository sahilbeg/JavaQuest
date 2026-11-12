package jq.m05.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TelemetryIngesterTest {

    @Test
    @DisplayName("parse builds the right record for each kind")
    void parsesEachKind() {
        assertEquals(new Battery("B-1042", 80), TelemetryIngester.parse("B-1042|BATTERY|80"), "battery line");
        assertEquals(new Gps("B-1042", 51.51, -0.12), TelemetryIngester.parse("B-1042|GPS|51.51,-0.12"), "GPS line");
        assertEquals(new Fault("B-2001", "E7"), TelemetryIngester.parse("B-2001|FAULT|E7"), "fault line");
    }

    @Test
    @DisplayName("parse rejects broken lines")
    void rejectsBrokenLines() {
        assertEquals(new Battery("B-1", 5), TelemetryIngester.parse("B-1|BATTERY|5"), "a good line must parse before bad ones are checked");
        for (String bad : List.of("B-3001|BATTERY|lots", "B-3001|SPEED|20", "B-3001|BATTERY", "B-3001|GPS|51.5")) {
            assertThrows(IllegalArgumentException.class, () -> TelemetryIngester.parse(bad),
                    "\"" + bad + "\" should throw IllegalArgumentException");
        }
    }

    @Test
    @DisplayName("a small batch gives the expected report")
    void smallBatch() throws Exception {
        IngestReport report = TelemetryIngester.ingest(List.of(
                "B-1042|BATTERY|80", "B-1042|BATTERY|15", "B-1042|GPS|51.51,-0.12",
                "B-2001|FAULT|E7", "B-2001|FAULT|E2", "B-3001|BATTERY|lots"), 2);
        assertEquals(Map.of("B-1042", 15), report.lowestBattery(), "B-1042's lowest level is 15; B-3001's line is bad");
        assertEquals(Map.of("B-2001", 2), report.faults(), "B-2001 sent two faults");
        assertEquals(1, report.gpsFixes(), "one GPS line");
        assertEquals(1, report.badLines(), "\"lots\" is not a number");
        assertEquals(List.of("B-1042"), TelemetryIngester.needsCharge(report, 20), "15 is under 20");
    }

    @Test
    @DisplayName("rush hour: 40,000 lines on 8 workers lose nothing")
    void rushHour() throws Exception {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            String bike = "B-" + (1000 + i % 50);
            lines.add(bike + "|BATTERY|" + (i % 100));
            lines.add(bike + "|FAULT|E" + (i % 9));
            lines.add(bike + "|GPS|51.5,-0.1");
            lines.add(bike + "|BATTERY|broken");
        }
        IngestReport report = TelemetryIngester.ingest(lines, 8);
        assertEquals(50, report.faults().size(), "50 different bikes sent faults");
        int totalFaults = report.faults().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(10_000, totalFaults, "every fault line counts once; a lower number means lost updates");
        assertEquals(10_000, report.gpsFixes(), "every GPS line counts once");
        assertEquals(10_000, report.badLines(), "every broken battery line counts once");
        assertEquals(0, report.lowestBattery().get("B-1000"), "B-1000 sends levels 0, 50, 0, 50 and so on");
        assertEquals(49, report.lowestBattery().get("B-1049"), "B-1049 sends levels 49, 99, 49 and so on");
    }
}
