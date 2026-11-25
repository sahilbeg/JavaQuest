package dev.cadence.content;

import java.nio.file.Path;
import java.util.Set;

/** Which files in a ticket's practice folders are the learner's code: Java for the Java course, web files for the web courses. */
public final class PracticeFiles {

    public static final Set<String> SOURCE_EXTENSIONS = Set.of(".java", ".js", ".jsx", ".mjs", ".ts", ".tsx", ".css", ".html");

    private PracticeFiles() {}

    public static boolean isSource(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 && SOURCE_EXTENSIONS.contains(name.substring(dot));
    }
}
