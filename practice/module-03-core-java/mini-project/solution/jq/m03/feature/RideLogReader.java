package jq.m03.feature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RideLogReader {

    public static ImportResult read(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<Ride> rides = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty()) {
                continue;
            }
            int lineNumber = i + 1;
            String[] parts = line.split(",");
            if (parts.length != 4) {
                errors.add("line " + lineNumber + ": expected 4 values");
                continue;
            }
            String type = parts[2].strip();
            if (!knownTypes().contains(type)) {
                errors.add("line " + lineNumber + ": unknown type: " + type);
                continue;
            }
            try {
                int minutes = Integer.parseInt(parts[3].strip());
                rides.add(new Ride(parts[0].strip(), parts[1].strip(), type, minutes));
            } catch (NumberFormatException e) {
                errors.add("line " + lineNumber + ": bad minutes: " + parts[3].strip());
            }
        }
        return new ImportResult(rides, errors);
    }

    public static List<String> knownTypes() {
        return List.of("classic", "ebike", "cargo");
    }
}
