package jq.m03.feature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RideLogReader {

    public static ImportResult read(Path file) throws IOException {
        // TODO 2: read every line, skip the header and blank lines, collect rides and error messages
        return new ImportResult(new ArrayList<>(), new ArrayList<>());
    }

    public static List<String> knownTypes() {
        return List.of("classic", "ebike", "cargo");
    }
}
