package jq.m03.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogImporterTest {

    @Test
    @DisplayName("parseMinutes turns \"25\" into 25 and \"twenty\" into -1")
    void parseMinutes() {
        assertEquals(25, LogImporter.parseMinutes("25"),
                "parseMinutes(\"25\") should return 25 using Integer.parseInt");
        assertEquals(-1, LogImporter.parseMinutes("twenty"),
                "parseMinutes(\"twenty\") should catch NumberFormatException and return -1");
    }

    @Test
    @DisplayName("parseLine builds a Ride from a good line")
    void parseGoodLine() {
        assertEquals(new Ride("B-1042", "Riverside", "ebike", 25),
                LogImporter.parseLine("B-1042,Riverside,ebike,25"),
                "parseLine(\"B-1042,Riverside,ebike,25\") should return new Ride(\"B-1042\", \"Riverside\", \"ebike\", 25)");
    }

    @Test
    @DisplayName("parseLine throws IllegalArgumentException when the minutes are not a number")
    void badMinutesThrow() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> LogImporter.parseLine("B-1042,Riverside,ebike,twenty"),
                "parseLine should throw IllegalArgumentException when the minutes say \"twenty\"");
        assertTrue(e.getMessage() != null && e.getMessage().contains("twenty"),
                "The exception message should include the bad minutes \"twenty\" so support can find it");
    }

    @Test
    @DisplayName("parseLine throws IllegalArgumentException when a line does not have 4 parts")
    void shortLineThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> LogImporter.parseLine("B-3300,Uptown"),
                "parseLine(\"B-3300,Uptown\") should throw IllegalArgumentException: it has 2 parts, not 4");
        assertTrue(e.getMessage() != null && e.getMessage().contains("B-3300,Uptown"),
                "The exception message should include the whole bad line");
    }

    @Test
    @DisplayName("importAll keeps the good rides and counts the bad lines")
    void importSkipsBadLines() {
        ImportResult result = LogImporter.importAll(List.of(
                "B-1042,Riverside,ebike,25",
                "B-1042,Riverside,ebike,twenty",
                "B-2001,Harbor,classic,12",
                "B-3300,Uptown"));
        assertEquals(List.of(
                        new Ride("B-1042", "Riverside", "ebike", 25),
                        new Ride("B-2001", "Harbor", "classic", 12)),
                result.rides(),
                "importAll should keep the 2 good rides, in order, and skip the broken lines");
        assertEquals(2, result.badLines(),
                "importAll should count 2 bad lines: one with \"twenty\" and one with only 2 parts");
    }
}
