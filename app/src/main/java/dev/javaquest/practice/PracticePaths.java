package dev.javaquest.practice;

import java.nio.file.Path;
import java.util.Optional;

/** Maps a changed file to the topic it belongs to: practice/&lt;module&gt;/&lt;topic&gt;/src/**&#47;*.java. */
public final class PracticePaths {

    private PracticePaths() {}

    public static Optional<String> topicIdForSourceFile(Path practiceRoot, Path changedFile) {
        Path relative = practiceRoot.toAbsolutePath().normalize().relativize(changedFile.toAbsolutePath().normalize());
        boolean javaSourceInSrc = relative.getNameCount() >= 4
                && relative.getName(2).toString().equals("src")
                && relative.getFileName().toString().endsWith(".java");
        if (!javaSourceInSrc) {
            return Optional.empty();
        }
        return Optional.of(relative.getName(0) + "/" + relative.getName(1));
    }
}
