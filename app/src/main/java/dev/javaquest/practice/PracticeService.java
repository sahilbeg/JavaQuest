package dev.javaquest.practice;

import dev.javaquest.content.Topic;
import dev.javaquest.practice.PracticeResult.Source;
import dev.javaquest.runner.PracticeRunner;
import dev.javaquest.web.EventHub;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class PracticeService {

    private static final int MAX_PARALLEL_RUNS = 2;

    private final PracticeRunner runner;
    private final EventHub events;
    private final Semaphore parallelRuns = new Semaphore(MAX_PARALLEL_RUNS);
    private final Map<String, ReentrantLock> topicLocks = new ConcurrentHashMap<>();
    private final Map<String, PracticeResult> latest = new ConcurrentHashMap<>();

    public PracticeService(PracticeRunner runner, EventHub events) {
        this.runner = runner;
        this.events = events;
    }

    public PracticeResult submit(Topic topic, Source source) throws IOException, InterruptedException {
        ReentrantLock lock = topicLocks.computeIfAbsent(topic.id(), id -> new ReentrantLock());
        lock.lockInterruptibly();
        parallelRuns.acquire();
        try {
            events.publish("run-started", Map.of("topicId", topic.id(), "source", source));
            var result = new PracticeResult(topic.id(), source,
                    runner.run(topic.practiceSources(), topic.practiceTests()));
            latest.put(topic.id(), result);
            events.publish("run-finished", result);
            return result;
        } finally {
            parallelRuns.release();
            lock.unlock();
        }
    }

    public Optional<PracticeResult> latest(String topicId) {
        return Optional.ofNullable(latest.get(topicId));
    }
}
