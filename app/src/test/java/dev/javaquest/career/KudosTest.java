package dev.javaquest.career;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class KudosTest {

    private static Kudos.Stats stats(Set<String> mastered, int noHints, int perfect, int bugs, int ide, int streak, int reviews) {
        return new Kudos.Stats(mastered, noHints > 0, noHints, perfect, bugs, ide, streak, reviews,
                Map.of("module-00-foundations", List.of(Kudos.HELLO_WORLD_TOPIC, "module-00-foundations/topic-06-git-basics")));
    }

    @Test
    void nothingIsEarnedAtTheStart() {
        assertEquals(Set.of(), Kudos.earned(stats(Set.of(), 0, 0, 0, 0, 0, 0)));
    }

    @Test
    void thresholdsAreInclusive() {
        var earned = Kudos.earned(stats(Set.of(), 10, 10, 20, 10, 7, 50));
        assertEquals(Set.of("first-green", "no-hints", "perfectionist", "bug-hunter", "ide-native", "on-fire", "memory-palace"), earned);
    }

    @Test
    void oneBelowEachThresholdEarnsNothingForIt() {
        var earned = Kudos.earned(stats(Set.of(), 9, 9, 19, 9, 6, 49));
        assertEquals(Set.of("first-green"), earned);
    }

    @Test
    void projectShipsOnlyWhenEveryUnitIsMastered() {
        assertEquals(Set.of("hello-world"), Kudos.earned(stats(Set.of(Kudos.HELLO_WORLD_TOPIC), 0, 0, 0, 0, 0, 0)));
        var all = Set.of(Kudos.HELLO_WORLD_TOPIC, "module-00-foundations/topic-06-git-basics");
        assertTrue(Kudos.earned(stats(all, 0, 0, 0, 0, 0, 0)).contains("shipped-module-00-foundations"));
    }

    @Test
    void thirtyDayStreakAlsoKeepsTheSevenDayKudos() {
        assertTrue(Kudos.earned(stats(Set.of(), 0, 0, 0, 0, 30, 0)).containsAll(Set.of("on-fire", "unstoppable")));
    }
}
