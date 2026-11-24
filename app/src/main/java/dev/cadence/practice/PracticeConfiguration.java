package dev.cadence.practice;

import dev.cadence.CadenceProperties;
import dev.cadence.content.ContentCatalog;
import dev.cadence.runner.PracticeRunner;
import dev.cadence.runner.RunLimits;
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
    dev.cadence.content.CourseLibrary courseLibrary(CadenceProperties properties) throws IOException {
        return dev.cadence.content.CourseLibrary.load(properties.root());
    }

    /** Every topic of every course, for finding a topic by id. Course order and progress use {@code Courses}. */
    @Bean
    ContentCatalog contentCatalog(dev.cadence.content.CourseLibrary library) {
        return library.library();
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    PracticeRunner practiceRunner(CadenceProperties properties) throws IOException {
        var runner = properties.runner();
        var limits = new RunLimits(runner.timeout(), runner.heapMegabytes(), runner.maxOutputBytes());
        return new PracticeRunner(PracticeRunner.currentJavaExecutable(), readClasspath(properties.runnerClasspathFile()), limits);
    }

    private static List<Path> readClasspath(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("Test classpath file " + file + " is missing. Start Cadence with ./run.sh, which creates it.");
        }
        return Arrays.stream(Files.readString(file).strip().split(java.io.File.pathSeparator))
                .filter(entry -> !entry.isBlank())
                .map(Path::of)
                .toList();
    }
}
