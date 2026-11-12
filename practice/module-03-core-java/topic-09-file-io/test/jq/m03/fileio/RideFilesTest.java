package jq.m03.fileio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RideFilesTest {

    @TempDir
    Path dir;

    private File fileWith(String text) throws IOException {
        File file = dir.resolve("rides.csv").toFile();
        Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    @DisplayName("read turns each CSV line into a Ride")
    void readsRides() throws IOException {
        File file = fileWith("B-1042,Riverside,ebike,25\nB-2001,Harbor,classic,10\n");
        List<Ride> rides = RideFiles.read(file);
        assertEquals(2, rides.size(), "read should return 2 rides for a file with 2 lines");
        assertEquals(new Ride("B-1042", "Riverside", "ebike", 25), rides.get(0),
                "the first line B-1042,Riverside,ebike,25 should become Ride[B-1042, Riverside, ebike, 25]");
        assertEquals(new Ride("B-2001", "Harbor", "classic", 10), rides.get(1),
                "the second line should become Ride[B-2001, Harbor, classic, 10]");
    }

    @Test
    @DisplayName("read skips blank lines")
    void skipsBlankLines() throws IOException {
        File file = fileWith("B-1042,Riverside,ebike,25\n\n   \nB-3300,Uptown,cargo,40\n");
        List<Ride> rides = RideFiles.read(file);
        assertEquals(2, rides.size(), "read should skip blank lines and return only the 2 real rides");
        assertEquals(40, rides.get(1).minutes(), "the cargo ride on Uptown should have 40 minutes");
    }

    @Test
    @DisplayName("write puts one CSV line per ride in the file")
    void writesCsvLines() throws IOException {
        File file = dir.resolve("out.csv").toFile();
        RideFiles.write(file, List.of(
                new Ride("B-1042", "Riverside", "ebike", 25),
                new Ride("B-0007", "Old Mill", "classic", 12)));
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        assertEquals(List.of("B-1042,Riverside,ebike,25", "B-0007,Old Mill,classic,12"), lines,
                "write should put each ride on its own line as bikeId,station,type,minutes");
    }

    @Test
    @DisplayName("Rides written by write can be read back by read")
    void roundTrip() throws IOException {
        File file = dir.resolve("round.csv").toFile();
        List<Ride> rides = List.of(new Ride("B-5555", "Harbor", "cargo", 30));
        RideFiles.write(file, rides);
        assertEquals(rides, RideFiles.read(file),
                "reading the file you just wrote should give back the same list of rides");
    }

    @Test
    @DisplayName("countLines counts every line, blank lines included")
    void countsLines() throws IOException {
        File file = fileWith("a\n\nb\nc\n");
        assertEquals(4, RideFiles.countLines(file),
                "countLines should return 4: the lines a, an empty line, b and c");
    }
}
