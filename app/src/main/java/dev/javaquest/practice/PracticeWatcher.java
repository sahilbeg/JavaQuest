package dev.javaquest.practice;

import dev.javaquest.JavaQuestProperties;
import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.Topic;
import dev.javaquest.practice.PracticeResult.Source;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/** Re-runs a topic's tests when the learner saves one of its source files in any editor. */
@Component
public class PracticeWatcher implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(PracticeWatcher.class);
    private static final Duration DEBOUNCE = Duration.ofMillis(500);

    private final ContentCatalog catalog;
    private final PracticeService practice;
    private final Path practiceRoot;
    private final Map<WatchKey, Path> watchedDirs = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private WatchService watchService;
    private volatile boolean running;

    public PracticeWatcher(ContentCatalog catalog, PracticeService practice, JavaQuestProperties properties) {
        this.catalog = catalog;
        this.practice = practice;
        this.practiceRoot = properties.practiceDir();
    }

    @Override
    public void start() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            for (Topic topic : catalog.topics()) {
                if (Files.isDirectory(topic.practiceSources())) {
                    registerTree(topic.practiceSources());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not watch " + practiceRoot, e);
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
        running = true;
        Thread.ofVirtual().name("practice-watcher").start(this::watchLoop);
    }

    private void registerTree(Path root) throws IOException {
        try (Stream<Path> dirs = Files.walk(root)) {
            for (Path dir : dirs.filter(Files::isDirectory).toList()) {
                WatchKey key = dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
                watchedDirs.put(key, dir);
            }
        }
    }

    private void watchLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                return;
            }
            Path dir = watchedDirs.get(key);
            for (WatchEvent<?> event : key.pollEvents()) {
                if (dir != null && event.context() instanceof Path name) {
                    onChange(dir.resolve(name));
                }
            }
            if (!key.reset()) {
                watchedDirs.remove(key);
            }
        }
    }

    private void onChange(Path changed) {
        if (Files.isDirectory(changed)) {
            try {
                registerTree(changed);
            } catch (IOException e) {
                log.warn("Could not watch new folder {}", changed, e);
            }
            return;
        }
        PracticePaths.topicIdForSourceFile(practiceRoot, changed)
                .flatMap(catalog::topic)
                .ifPresent(this::scheduleRun);
    }

    private void scheduleRun(Topic topic) {
        pending.compute(topic.id(), (id, previous) -> {
            if (previous != null) {
                previous.cancel(false);
            }
            return scheduler.schedule(() -> runTests(topic), DEBOUNCE.toMillis(), TimeUnit.MILLISECONDS);
        });
    }

    private void runTests(Topic topic) {
        try {
            practice.submit(topic, Source.IDE);
        } catch (IOException e) {
            log.warn("Test run for {} failed to start", topic.id(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            log.debug("Watch service did not close cleanly", e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
