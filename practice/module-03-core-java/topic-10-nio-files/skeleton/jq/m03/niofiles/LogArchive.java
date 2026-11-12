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
        // TODO 1: add csvLine plus "\n" to the end of the log with Files.writeString, using UTF_8, CREATE and APPEND
    }

    public static List<String> lines(Path log) throws IOException {
        // TODO 2: if the log does not exist, return an empty list; otherwise return Files.readAllLines(log, UTF_8)
        return null;
    }

    public static Path archive(Path log, Path archiveDir, String dateLabel) throws IOException {
        // TODO 3: create archiveDir with Files.createDirectories
        // TODO 4: build archiveDir/rides-<dateLabel>.csv with resolve, move the log there and return the new path
        return null;
    }

    public static List<String> listArchives(Path archiveDir) throws IOException {
        List<String> names = new ArrayList<>();
        // TODO 5: loop over Files.newDirectoryStream(archiveDir) and add each getFileName().toString()
        // TODO 6: sort the names with Collections.sort before returning them
        return names;
    }
}
