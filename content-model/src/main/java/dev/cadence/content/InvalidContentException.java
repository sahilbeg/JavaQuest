package dev.cadence.content;

import java.nio.file.Path;

public class InvalidContentException extends RuntimeException {

    public InvalidContentException(Path file, String problem) {
        super("Invalid content in " + file + ": " + problem);
    }
}
