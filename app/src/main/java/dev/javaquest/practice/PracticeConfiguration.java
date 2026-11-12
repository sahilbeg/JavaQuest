package dev.javaquest.practice;

import dev.javaquest.JavaQuestProperties;
import dev.javaquest.content.ContentCatalog;
import dev.javaquest.runner.PracticeRunner;
import dev.javaquest.runner.RunLimits;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PracticeConfiguration {

    @Bean
    ContentCatalog contentCatalog(JavaQuestProperties properties) throws IOException {
        return ContentCatalog.load(properties.contentDir(), properties.practiceDir());
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    PracticeRunner practiceRunner(JavaQuestProperties properties) throws IOException {
        var runner = properties.runner();
        var limits = new RunLimits(runner.timeout(), runner.heapMegabytes(), runner.maxOutputBytes());
        return new PracticeRunner(PracticeRunner.currentJavaExecutable(), readClasspath(properties.runnerClasspathFile()), limits);
    }

    private static List<Path> readClasspath(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("Test classpath file " + file + " is missing. Start JavaQuest with ./run.sh, which creates it.");
        }
        return Arrays.stream(Files.readString(file).strip().split(java.io.File.pathSeparator))
                .filter(entry -> !entry.isBlank())
                .map(Path::of)
                .toList();
    }
}
