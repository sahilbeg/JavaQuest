package jq.m03.niofiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogArchive {

    public static void appendRide(Path log, String csvLine) throws IOException {
        Files.writeString(log, csvLine + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static List<String> lines(Path log) throws IOException {
        if (!Files.exists(log)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(log, StandardCharsets.UTF_8);
    }

    public static Path archive(Path log, Path archiveDir, String dateLabel) throws IOException {
        Files.createDirectories(archiveDir);
        Path target = archiveDir.resolve("rides-" + dateLabel + ".csv");
        Files.move(log, target);
        return target;
    }

    public static List<String> listArchives(Path archiveDir) throws IOException {
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(archiveDir)) {
            for (Path entry : entries) {
                names.add(entry.getFileName().toString());
            }
        }
        Collections.sort(names);
        return names;
    }
}
