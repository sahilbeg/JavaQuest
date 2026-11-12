package dev.javaquest.practice;

import dev.javaquest.JavaQuestProperties;
import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.Topic;
import dev.javaquest.practice.PracticeResult.Source;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryWatcher;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Re-runs a topic's tests when the learner saves one of its source files in any editor. Uses native file
 * events (FSEvents on macOS); the JDK WatchService only polls there and took 2.2 to 2.5 s to notice a save.
 */
@Component
public class PracticeWatcher implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(PracticeWatcher.class);
    private static final Duration DEBOUNCE = Duration.ofMillis(300);

    private final ContentCatalog catalog;
    private final PracticeService practice;
    private final Path practiceRoot;
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private DirectoryWatcher watcher;
    private volatile boolean running;

    public PracticeWatcher(ContentCatalog catalog, PracticeService practice, JavaQuestProperties properties) {
        this.catalog = catalog;
        this.practice = practice;
        this.practiceRoot = properties.practiceDir().toAbsolutePath().normalize();
    }

    @Override
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
        try {
            watcher = DirectoryWatcher.builder()
                    .path(practiceRoot)
                    .fileHashing(false)
                    .listener(this::onEvent)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Could not watch " + practiceRoot, e);
        }
        watcher.watchAsync();
        running = true;
    }

    private void onEvent(DirectoryChangeEvent event) {
        if (event.eventType() == DirectoryChangeEvent.EventType.DELETE || event.isDirectory()) {
            return;
        }
        PracticePaths.topicIdForSourceFile(practiceRoot, event.path())
                .filter(topicId -> !practice.isEchoOfBrowserWrite(topicId))
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
        try {
            if (watcher != null) {
                watcher.close();
            }
        } catch (IOException e) {
            log.debug("Watcher did not close cleanly", e);
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
