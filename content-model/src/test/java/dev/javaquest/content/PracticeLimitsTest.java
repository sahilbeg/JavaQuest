package dev.javaquest.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.javaquest.content.TopicMeta.Practice;
import dev.javaquest.content.TopicMeta.PracticeKind;
import dev.javaquest.content.TopicMeta.RunnerKind;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class PracticeLimitsTest {

    private static final Duration DEFAULT = Duration.ofSeconds(10);

    @Test
    void plainTopicsKeepTheDefaults() {
        var practice = new Practice(PracticeKind.CODE, RunnerKind.PLAIN, null, null);
        assertEquals(DEFAULT, practice.timeout(DEFAULT));
        assertEquals(256, practice.heapMegabytes(256));
    }

    @Test
    void timeoutSecondsWins() {
        assertEquals(Duration.ofSeconds(30), new Practice(PracticeKind.CODE, null, null, 30).timeout(DEFAULT));
        assertEquals(Duration.ofSeconds(90), new Practice(PracticeKind.CODE, RunnerKind.SPRING, null, 90).timeout(DEFAULT));
    }

    @Test
    void springTopicsGetMoreTimeAndHeap() {
        var spring = new Practice(PracticeKind.CODE, RunnerKind.SPRING, null, null);
        assertEquals(Practice.SPRING_TIMEOUT, spring.timeout(DEFAULT));
        assertEquals(Duration.ofSeconds(120), spring.timeout(Duration.ofSeconds(120)), "a longer default is kept");
        assertEquals(Practice.SPRING_HEAP_MEGABYTES, spring.heapMegabytes(256));
        assertEquals(1024, spring.heapMegabytes(1024), "a bigger default is kept");
    }

    @Test
    void timeoutSecondsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Practice(PracticeKind.CODE, null, null, 0));
    }
}
