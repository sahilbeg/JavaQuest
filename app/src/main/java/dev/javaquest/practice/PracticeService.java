package dev.javaquest.practice;

import dev.javaquest.content.Topic;
import dev.javaquest.progress.ProgressService;
import dev.javaquest.practice.PracticeResult.Source;
import dev.javaquest.runner.PracticeRunner;
import dev.javaquest.web.EventHub;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class PracticeService {

    private static final int MAX_PARALLEL_RUNS = 2;
    private static final Duration BROWSER_WRITE_ECHO = Duration.ofSeconds(3);

    private final PracticeRunner runner;
    private final EventHub events;
    private final Clock clock;
    private final ProgressService progress;
    private final Semaphore parallelRuns = new Semaphore(MAX_PARALLEL_RUNS);
    private final Map<String, ReentrantLock> topicLocks = new ConcurrentHashMap<>();
    private final Map<String, Instant> browserWrites = new ConcurrentHashMap<>();

    public PracticeService(PracticeRunner runner, EventHub events, Clock clock, ProgressService progress) {
        this.runner = runner;
        this.events = events;
        this.clock = clock;
        this.progress = progress;
    }

    public PracticeResult submit(Topic topic, Source source) throws IOException, InterruptedException {
        ReentrantLock lock = topicLocks.computeIfAbsent(topic.id(), id -> new ReentrantLock());
        lock.lockInterruptibly();
        parallelRuns.acquire();
        try {
            events.publish("run-started", Map.of("topicId", topic.id(), "source", source));
            var result = new PracticeResult(topic.id(), source,
                    runner.run(topic.practiceSources(), topic.practiceTests()));
            progress.recordTestRun(topic, result);
            events.publish("run-finished", result);
            return result;
        } finally {
            parallelRuns.release();
            lock.unlock();
        }
    }

    public ProgramRun runMain(Topic topic, String mainClass) throws IOException, InterruptedException {
        parallelRuns.acquire();
        try {
            return new ProgramRun(runner.runMain(topic.practiceSources(), mainClass));
        } finally {
            parallelRuns.release();
        }
    }

    public ProgramRun runSnippet(String code) throws IOException, InterruptedException {
        parallelRuns.acquire();
        try {
            return new ProgramRun(runner.runSnippet(code));
        } finally {
            parallelRuns.release();
        }
    }

    public Optional<PracticeResult> latest(String topicId) {
        return progress.latestRun(topicId);
    }

    /** Puts the untouched skeleton back into src/, replacing the learner's edits. */
    public void resetToSkeleton(Topic topic) throws IOException {
        Path src = topic.practiceSources();
        Path skeleton = topic.practiceSkeleton();
        browserWrites.put(topic.id(), clock.instant());
        try (Stream<Path> old = Files.walk(src)) {
            for (Path file : old.filter(p -> p.toString().endsWith(".java")).toList()) {
                Files.delete(file);
            }
        }
        try (Stream<Path> fresh = Files.walk(skeleton)) {
            for (Path file : fresh.filter(Files::isRegularFile).toList()) {
                Path target = src.resolve(skeleton.relativize(file).toString());
                Files.createDirectories(target.getParent());
                Files.copy(file, target);
            }
        }
    }

    public List<String> sourceFiles(Topic topic) throws IOException {
        Path root = topic.practiceSources();
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(p -> p.toString().endsWith(".java"))
                    .map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .sorted()
                    .toList();
        }
    }

    public String readSource(Topic topic, String relativePath) throws IOException {
        return Files.readString(existingSource(topic, relativePath), StandardCharsets.UTF_8);
    }

    public void writeSource(Topic topic, String relativePath, String content) throws IOException {
        Path file = existingSource(topic, relativePath);
        browserWrites.put(topic.id(), clock.instant());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    /** True right after the browser saved this topic, so the file watcher does not report that save as an IDE edit. */
    public boolean isEchoOfBrowserWrite(String topicId) {
        Instant written = browserWrites.get(topicId);
        return written != null && Duration.between(written, clock.instant()).compareTo(BROWSER_WRITE_ECHO) < 0;
    }

    private static Path existingSource(Topic topic, String relativePath) {
        Path root = topic.practiceSources().toAbsolutePath().normalize();
        Path file = root.resolve(relativePath).normalize();
        if (!file.startsWith(root) || !file.toString().endsWith(".java") || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Not a practice source file: " + relativePath);
        }
        return file;
    }
}
