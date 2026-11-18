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
                Map.of("module-00-foundations", List.of(Kudos.HELLO_WORLD_TOPIC, "module-00-foundations/topic-06-git-basics")), 0, 0);
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

    @Test
    void incidentAndCapstoneKudos() {
        var base = stats(Set.of(Kudos.CAPSTONE_TOPIC), 0, 0, 0, 0, 0, 0);
        var withBosses = new Kudos.Stats(base.masteredTopics(), false, 0, 0, 0, 0, 0, 0, base.unitsByModule(), 1, 0);
        assertEquals(Set.of("java-pro", "boss-slayer"), Kudos.earned(withBosses));
        var withGold = new Kudos.Stats(Set.of(), false, 0, 0, 0, 0, 0, 0, Map.of(), 1, 1);
        assertEquals(Set.of("boss-slayer", "gold-standard"), Kudos.earned(withGold));
    }

    @Test
    void fullTimeOfferNeedsEveryUnitIncludingTheCapstone() {
        var units = Map.of(
                "module-00-foundations", List.of(Kudos.HELLO_WORLD_TOPIC),
                "module-10-professional", List.of("module-10-professional/boss", Kudos.CAPSTONE_TOPIC));
        var all = Set.of(Kudos.HELLO_WORLD_TOPIC, "module-10-professional/boss", Kudos.CAPSTONE_TOPIC);
        var done = new Kudos.Stats(all, false, 0, 0, 0, 0, 0, 0, units, 0, 0);
        assertTrue(Kudos.earned(done).containsAll(Set.of("java-pro", "full-time")));

        var capstoneOnly = new Kudos.Stats(Set.of(Kudos.CAPSTONE_TOPIC), false, 0, 0, 0, 0, 0, 0, units, 0, 0);
        assertTrue(Kudos.earned(capstoneOnly).contains("java-pro"));
        assertTrue(!Kudos.earned(capstoneOnly).contains("full-time"), "one open ticket keeps the full-time kudos back");

        var noCapstoneModule = new Kudos.Stats(Set.of(Kudos.HELLO_WORLD_TOPIC), false, 0, 0, 0, 0, 0, 0,
                Map.of("module-00-foundations", List.of(Kudos.HELLO_WORLD_TOPIC)), 0, 0);
        assertTrue(!Kudos.earned(noCapstoneModule).contains("full-time"), "a course without the capstone is never finished");
    }
}
