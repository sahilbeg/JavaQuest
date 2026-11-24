package dev.cadence.practice;

import java.nio.file.Path;
import java.util.Optional;

/** Maps a changed file to the topic it belongs to: &lt;practice root&gt;/&lt;module&gt;/&lt;topic&gt;/src/** (Java or web files). */
public final class PracticePaths {

    private PracticePaths() {}

    public static Optional<String> topicIdForSourceFile(Path practiceRoot, Path changedFile) {
        Path relative = practiceRoot.toAbsolutePath().normalize().relativize(changedFile.toAbsolutePath().normalize());
        boolean sourceInSrc = relative.getNameCount() >= 4
                && !relative.startsWith("..")
                && relative.getName(2).toString().equals("src")
                && dev.cadence.content.PracticeFiles.isSource(relative);
        if (!sourceInSrc) {
            return Optional.empty();
        }
        return Optional.of(relative.getName(0) + "/" + relative.getName(1));
    }
}
