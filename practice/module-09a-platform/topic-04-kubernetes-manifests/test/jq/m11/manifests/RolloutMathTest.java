package jq.m11.manifests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RolloutMathTest {

    /** Calls RolloutMath.plan, but gives up after 2 seconds, in case the loop never ends. */
    static List<PodCounts> plan(int replicas, String maxSurge, String maxUnavailable) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), () -> RolloutMath.plan(replicas, maxSurge, maxUnavailable),
                "plan(" + replicas + ", " + maxSurge + ", " + maxUnavailable + ") should finish: each pass must add or remove a Pod");
    }

    @Test
    @DisplayName("The default 25% surge rounds up and 25% unavailable rounds down")
    void defaultPercentagesRound() {
        assertEquals(new RolloutLimits(3, 2, 13, 8), RolloutMath.limits(10, "25%", "25%"),
                "25% of 10 is 2.5: maxSurge rounds up to 3, maxUnavailable rounds down to 2, so 8 to 13 Pods");
        assertEquals(new RolloutLimits(1, 1, 5, 3), RolloutMath.limits(4, "25%", "25%"),
                "25% of 4 is exactly 1 for both: 3 to 5 Pods");
        assertEquals(new RolloutLimits(1, 0, 4, 3), RolloutMath.limits(3, "25%", "25%"),
                "25% of 3 is 0.75: surge rounds up to 1, unavailable rounds down to 0");
    }

    @Test
    @DisplayName("Whole numbers are used as they are, and resolve() reads both forms")
    void wholeNumbersAndResolve() {
        assertEquals(new RolloutLimits(2, 0, 32, 30), RolloutMath.limits(30, "2", "0"),
                "maxSurge 2 and maxUnavailable 0 with 30 replicas: 30 to 32 Pods");
        assertEquals(2, RolloutMath.resolve("50%", 3, true), "50% of 3 is 1.5, rounded up to 2");
        assertEquals(1, RolloutMath.resolve("50%", 3, false), "50% of 3 is 1.5, rounded down to 1");
        assertEquals(4, RolloutMath.resolve("4", 3, false), "a whole number is not a percentage: 4 stays 4");
        assertEquals(1, RolloutMath.resolve("1%", 30, true), "1% of 30 is 0.3, and any fraction rounds up to 1");
    }

    @Test
    @DisplayName("Both set to 0 is rejected, and so are bad values and fewer than 1 replica")
    void impossibleSettingsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> RolloutMath.limits(3, "0", "0"),
                "with maxSurge 0 and maxUnavailable 0, no Pod could ever be replaced");
        assertThrows(IllegalArgumentException.class, () -> RolloutMath.limits(3, "0%", "0"),
                "0% is still 0: reject it");
        assertThrows(IllegalArgumentException.class, () -> RolloutMath.limits(3, "0", "0%"),
                "0% is still 0: reject it");
        assertThrows(IllegalArgumentException.class, () -> RolloutMath.limits(3, "-1", "1"),
                "a negative maxSurge is not allowed");
        assertThrows(IllegalArgumentException.class, () -> RolloutMath.limits(3, "1", "abc"),
                "abc is neither a whole number nor a percentage");
        assertThrows(IllegalArgumentException.class, () -> RolloutMath.limits(3, "1", "150%"),
                "maxUnavailable cannot be more than 100%");
        assertThrows(IllegalArgumentException.class, () -> RolloutMath.limits(0, "1", "1"),
                "a rollout needs at least 1 replica");
    }

    @Test
    @DisplayName("If rounding makes both 0, maxUnavailable becomes 1; and it never exceeds replicas")
    void roundingFencepostAndCap() {
        assertEquals(new RolloutLimits(0, 1, 3, 2), RolloutMath.limits(3, "0", "25%"),
                "25% of 3 rounds down to 0, and maxSurge is 0 too: Kubernetes then allows 1 unavailable Pod");
        assertEquals(new RolloutLimits(0, 1, 1, 0), RolloutMath.limits(1, "0%", "50%"),
                "50% of 1 rounds down to 0, so maxUnavailable becomes 1 and the only Pod may go");
        assertEquals(new RolloutLimits(0, 2, 2, 0), RolloutMath.limits(2, "0", "5"),
                "maxUnavailable 5 with only 2 replicas is capped at 2");
    }

    @Test
    @DisplayName("Plan with maxSurge 1 and maxUnavailable 0: add one new Pod, then remove one old Pod")
    void planSurgeOnly() {
        List<PodCounts> expected = List.of(new PodCounts(3, 0), new PodCounts(3, 1), new PodCounts(2, 1),
                new PodCounts(2, 2), new PodCounts(1, 2), new PodCounts(1, 3), new PodCounts(0, 3));
        assertEquals(expected, plan(3, "1", "0"),
                "each step either starts new Pods or stops old ones, never going above 4 Pods or below 3");
    }

    @Test
    @DisplayName("Plan with maxSurge 0 and maxUnavailable 1: remove one old Pod first, then add one new Pod")
    void planUnavailableOnly() {
        List<PodCounts> expected = List.of(new PodCounts(3, 0), new PodCounts(2, 0), new PodCounts(2, 1),
                new PodCounts(1, 1), new PodCounts(1, 2), new PodCounts(0, 2), new PodCounts(0, 3));
        assertEquals(expected, plan(3, "0", "1"),
                "with no surge, an old Pod must stop before a new one can start");
    }

    @Test
    @DisplayName("Plan with 25% and 25% of 4 replicas, and every plan stays inside its limits")
    void planBothAndStaysInLimits() {
        assertEquals(List.of(new PodCounts(4, 0), new PodCounts(4, 1), new PodCounts(2, 1), new PodCounts(2, 3),
                        new PodCounts(0, 3), new PodCounts(0, 4)),
                plan(4, "25%", "25%"),
                "add as many new Pods as maxPods allows, then remove as many old Pods as minAvailable allows, and repeat");

        String[][] settings = {{"25%", "25%"}, {"1", "0"}, {"0", "1"}, {"50%", "0"}, {"0", "25%"}, {"100%", "0"}, {"3", "2"}};
        for (int replicas : new int[] {1, 2, 3, 7, 10, 30}) {
            for (String[] s : settings) {
                List<PodCounts> plan = plan(replicas, s[0], s[1]);
                assertTrue(plan.size() >= 2, "plan(" + replicas + ", " + s[0] + ", " + s[1] + ") should list the start, the end and every step between");
                RolloutLimits limits = RolloutMath.limits(replicas, s[0], s[1]);
                assertEquals(new PodCounts(replicas, 0), plan.getFirst(), "a plan starts with every Pod old");
                assertEquals(new PodCounts(0, replicas), plan.getLast(), "a plan ends with every Pod new");
                for (PodCounts step : plan) {
                    assertTrue(step.total() <= limits.maxPods() && step.total() >= limits.minAvailable(),
                            "plan(" + replicas + ", " + s[0] + ", " + s[1] + ") step " + step
                                    + " should stay between " + limits.minAvailable() + " and " + limits.maxPods() + " Pods");
                }
            }
        }
    }
}
