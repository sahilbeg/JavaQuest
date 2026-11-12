package jq.m03.niofiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LogArchiveTest {

    @TempDir
    Path dir;

    @Test
    @DisplayName("appendRide creates the log and adds lines to the end")
    void appendCreatesAndAppends() throws IOException {
        Path log = dir.resolve("rides.csv");
        LogArchive.appendRide(log, "B-1042,Riverside,ebike,25");
        LogArchive.appendRide(log, "B-2001,Harbor,classic,10");
        assertTrue(Files.exists(log), "appendRide should create rides.csv when it does not exist yet");
        assertEquals(List.of("B-1042,Riverside,ebike,25", "B-2001,Harbor,classic,10"),
                Files.readAllLines(log, StandardCharsets.UTF_8),
                "the log should hold both rides, each on its own line, in the order you added them");
    }

    @Test
    @DisplayName("lines returns every line of the log")
    void linesReadsFile() throws IOException {
        Path log = dir.resolve("rides.csv");
        Files.writeString(log, "B-1042,Riverside,ebike,25\nB-3300,Uptown,cargo,40\n", StandardCharsets.UTF_8);
        assertEquals(List.of("B-1042,Riverside,ebike,25", "B-3300,Uptown,cargo,40"), LogArchive.lines(log),
                "lines should return the 2 lines in the file");
    }

    @Test
    @DisplayName("lines returns an empty list when the log is missing")
    void linesMissingFile() throws IOException {
        List<String> result = LogArchive.lines(dir.resolve("missing.csv"));
        assertEquals(List.of(), result,
                "lines should return an empty list (never null) when the file does not exist");
    }

    @Test
    @DisplayName("archive moves the log into the archive folder with the date in its name")
    void archiveMovesLog() throws IOException {
        Path log = dir.resolve("rides.csv");
        Files.writeString(log, "B-1042,Riverside,ebike,25\n", StandardCharsets.UTF_8);
        Path archiveDir = dir.resolve("archive");
        Path moved = LogArchive.archive(log, archiveDir, "2026-11-11");
        assertEquals(archiveDir.resolve("rides-2026-11-11.csv"), moved,
                "archive should return archive/rides-2026-11-11.csv");
        assertTrue(moved != null && Files.exists(moved), "the archived file should exist in the archive folder");
        assertFalse(Files.exists(log), "the original rides.csv should be gone after a move");
        assertEquals(List.of("B-1042,Riverside,ebike,25"), Files.readAllLines(moved, StandardCharsets.UTF_8),
                "the archived file should keep the same ride lines");
    }

    @Test
    @DisplayName("listArchives returns the file names in sorted order")
    void listArchivesSorted() throws IOException {
        Path archiveDir = dir.resolve("archive");
        Files.createDirectories(archiveDir);
        Files.writeString(archiveDir.resolve("rides-2026-11-11.csv"), "x");
        Files.writeString(archiveDir.resolve("rides-2026-11-09.csv"), "x");
        Files.writeString(archiveDir.resolve("rides-2026-11-10.csv"), "x");
        assertEquals(List.of("rides-2026-11-09.csv", "rides-2026-11-10.csv", "rides-2026-11-11.csv"),
                LogArchive.listArchives(archiveDir),
                "listArchives should return just the file names, sorted from oldest date to newest");
    }
}
