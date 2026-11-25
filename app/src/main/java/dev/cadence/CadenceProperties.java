package dev.cadence;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cadence")
/**
 * @param unlockAll author mode: every ticket is open, for writing and reviewing content. Learners keep it off,
 *                  so tickets open one at a time.
 * @param codeDir   where ticket code is kept, or null for the repository's src/ folders (a learner). The tester and
 *                  author modes set it to a folder of their own, so they never change a learner's code.
 */
public record CadenceProperties(Path root, Path dataDir, Runner runner, boolean unlockAll, Path codeDir) {

    public record Runner(Duration timeout, int heapMegabytes, int maxOutputBytes, Path classpathFile) {}

    /** The folder the file watcher reads code changes from, for one pool: its own code folder, or the pool's practice/. */
    public Path codeRoot(dev.cadence.content.CourseLibrary.Pool pool) {
        return codeDir == null ? root.resolve(pool.practice()) : codeDir.resolve(pool.id());
    }

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
