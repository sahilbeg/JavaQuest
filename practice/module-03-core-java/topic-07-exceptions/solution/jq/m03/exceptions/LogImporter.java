package jq.m03.exceptions;

import java.util.ArrayList;
import java.util.List;

public class LogImporter {

    public static int parseMinutes(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Ride parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Expected 4 parts but got " + parts.length + ": " + line);
        }
        int minutes = parseMinutes(parts[3]);
        if (minutes == -1) {
            throw new IllegalArgumentException("Minutes are not a number: " + parts[3]);
        }
        return new Ride(parts[0], parts[1], parts[2], minutes);
    }

    public static ImportResult importAll(List<String> lines) {
        List<Ride> rides = new ArrayList<>();
        int badLines = 0;
        for (String line : lines) {
            try {
                rides.add(parseLine(line));
            } catch (IllegalArgumentException e) {
                badLines = badLines + 1;
                System.out.println("Skipped: " + e.getMessage());
            }
        }
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
