package dev.javaquest;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("javaquest")
/**
 * @param unlockAll author mode: every ticket is open, for writing and reviewing content. Learners keep it off,
 *                  so tickets open one at a time.
 */
public record JavaQuestProperties(Path root, Path dataDir, Runner runner, boolean unlockAll) {

    public record Runner(Duration timeout, int heapMegabytes, int maxOutputBytes, Path classpathFile) {}

    public Path contentDir() {
        return root.resolve("content");
    }

    public Path practiceDir() {
        return root.resolve("practice");
    }

    /** The learner's own team pictures (tsunade.png and so on), kept out of git. See avatars/README.md. */
    public Path avatarsDir() {
        return root.resolve("avatars");
    }

    public Path runnerClasspathFile() {
        return root.resolve(runner.classpathFile());
    }
}
