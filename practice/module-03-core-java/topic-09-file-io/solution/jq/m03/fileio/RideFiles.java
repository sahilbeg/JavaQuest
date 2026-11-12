package jq.m03.fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RideFiles {

    public static List<Ride> read(File file) throws IOException {
        List<Ride> rides = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                if (!line.isBlank()) {
                    String[] parts = line.split(",");
                    rides.add(new Ride(parts[0], parts[1], parts[2], Integer.parseInt(parts[3])));
                }
                line = reader.readLine();
            }
        }
        return rides;
    }

    public static void write(File file, List<Ride> rides) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (Ride r : rides) {
                writer.write(r.bikeId() + "," + r.station() + "," + r.type() + "," + r.minutes());
                writer.newLine();
            }
        }
    }

    public static int countLines(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }
}
