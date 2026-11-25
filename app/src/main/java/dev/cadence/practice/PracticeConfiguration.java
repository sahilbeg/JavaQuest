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
        return dev.cadence.content.CourseLibrary.load(properties.root(), properties.codeDir());
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

    /** Java tickets run with javac and JUnit; web tickets with Node and Vitest in the web pool's practice folder. */
    @Bean
    Runners runners(PracticeRunner java, CadenceProperties properties, dev.cadence.content.CourseLibrary library) {
        var runner = properties.runner();
        var limits = new RunLimits(runner.timeout(), runner.heapMegabytes(), runner.maxOutputBytes());
        java.nio.file.Path workspace = library.pools().stream().filter(p -> p.id().equals("web")).findFirst()
                .map(p -> properties.root().resolve(p.practice()))
                .orElse(properties.root().resolve("tracks/web/practice"));
        return new Runners(java, new dev.cadence.runner.NodeRunner(dev.cadence.runner.NodeRunner.nodeOnPath(), workspace, limits));
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
