package jq.m03.exceptions;

import java.util.ArrayList;
import java.util.List;

public class LogImporter {

    public static int parseMinutes(String text) {
        // TODO 1: return Integer.parseInt(text) inside a try block
        // TODO 2: catch NumberFormatException and return -1
        return 0;
    }

    public static Ride parseLine(String line) {
        // TODO 3: split the line on "," and throw an IllegalArgumentException if there are not 4 parts
        // TODO 4: use parseMinutes; if it gives -1, throw an IllegalArgumentException that names the bad minutes
        // TODO 5: return a new Ride built from the 4 parts
        return null;
    }

    public static ImportResult importAll(List<String> lines) {
        List<Ride> rides = new ArrayList<>();
        int badLines = 0;
        // TODO 6: for each line, try to add parseLine(line) to rides
        // TODO 7: catch IllegalArgumentException, add 1 to badLines, and keep going
        return new ImportResult(rides, badLines);
    }

    public static void main(String[] args) {
        List<String> lines = List.of(
                "B-1042,Riverside,ebike,25",
                "B-1042,Riverside,ebike,twenty",
                "B-2001,Harbor,classic,12",
                "B-3300,Uptown");
        ImportResult result = importAll(lines);
        System.out.println("Imported " + result.rides().size() + " rides, skipped " + result.badLines() + " lines");
    }
}
