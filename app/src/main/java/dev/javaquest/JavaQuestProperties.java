package dev.javaquest;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("javaquest")
public record JavaQuestProperties(Path root, Runner runner) {

    public record Runner(Duration timeout, int heapMegabytes, int maxOutputBytes, Path classpathFile) {}

    public Path contentDir() {
        return root.resolve("content");
    }

    public Path practiceDir() {
        return root.resolve("practice");
    }

    public Path runnerClasspathFile() {
        return root.resolve(runner.classpathFile());
    }
}
