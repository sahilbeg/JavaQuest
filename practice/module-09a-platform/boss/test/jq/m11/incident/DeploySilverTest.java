package jq.m11.incident;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeploySilverTest {

    @Test
    @DisplayName("Silver: 30 pods with maxSurge 25% and maxUnavailable 0 roll out in 5 waves, always with 30 serving")
    void thirtyPodsSurgeOnly() {
        RolloutPlan plan = RolloutPlanner.plan(30, "25%", "0", 30);
        assertEquals(8, plan.surge(), "maxSurge 25% of 30 rounds up to 8 pods");
        assertEquals(0, plan.unavailable(), "maxUnavailable 0 is 0 pods");
        assertEquals(List.of(
                        new Wave(1, 8, 0, 30, 30, 8),
                        new Wave(2, 8, 8, 30, 22, 16),
                        new Wave(3, 8, 8, 30, 14, 24),
                        new Wave(4, 6, 8, 30, 6, 30),
                        new Wave(5, 0, 6, 30, 0, 30)),
                plan.waves(), "the waves should follow the README's rule; see its worked table for 30 pods");
    }

    @Test
    @DisplayName("Silver: 10 pods with 25% and 25% roll out in 3 waves, never with fewer than 8 serving")
    void tenPodsBoth() {
        RolloutPlan plan = RolloutPlanner.plan(10, "25%", "25%", 8);
        assertEquals(3, plan.surge(), "maxSurge 25% of 10 is 2.5, rounded up to 3");
        assertEquals(2, plan.unavailable(), "maxUnavailable 25% of 10 is 2.5, rounded down to 2");
        assertEquals(List.of(
                        new Wave(1, 5, 2, 8, 8, 5),
                        new Wave(2, 5, 5, 8, 3, 10),
                        new Wave(3, 0, 3, 10, 0, 10)),
                plan.waves(), "the waves should follow the README's rule for 10 pods");
        assertEquals(8, plan.lowestServing(), "at least 8 of 10 pods serve in every wave");
    }

    @Test
    @DisplayName("Silver: one pod at a time with maxSurge 0 and maxUnavailable 1")
    void onePodAtATime() {
        RolloutPlan plan = RolloutPlanner.plan(4, "0", "1", 3);
        assertEquals(List.of(
                        new Wave(1, 1, 1, 3, 3, 1),
                        new Wave(2, 1, 1, 3, 2, 2),
                        new Wave(3, 1, 1, 3, 1, 3),
                        new Wave(4, 1, 1, 3, 0, 4)),
                plan.waves(), "with no surge, each wave stops one old pod and starts one new one");
    }

    @Test
    @DisplayName("Silver: maxSurge and maxUnavailable both written as 0 are refused, as Kubernetes refuses them")
    void explicitZeroZeroIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> RolloutPlanner.plan(3, "0", "0", 0),
                "with no extra pod and no missing pod allowed, no pod could ever be replaced; throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> RolloutPlanner.plan(3, "0%", "0", 0),
                "0% is 0 too; throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Silver: when rounding makes both 0, one pod may be unavailable, as in Kubernetes, and the floor still applies")
    void roundingToZeroAllowsOne() {
        RolloutPlan plan = assertDoesNotThrow(() -> RolloutPlanner.plan(3, "0", "10%", 0),
                "10% of 3 rounds down to 0, and maxSurge is 0: README step 5 says to allow 1 unavailable pod, "
                        + "or the rollout makes no progress");
        assertEquals(0, plan.surge(), "maxSurge 0 is 0 pods");
        assertEquals(1, plan.unavailable(),
                "10% of 3 rounds down to 0, so both are 0 after rounding; Kubernetes then allows 1 unavailable pod");
        assertEquals(List.of(
                        new Wave(1, 1, 1, 2, 2, 1),
                        new Wave(2, 1, 1, 2, 1, 2),
                        new Wave(3, 1, 1, 2, 0, 3)),
                plan.waves(), "with 1 unavailable and no surge, the pods are replaced one at a time");
        assertThrows(UnsafeRolloutException.class, () -> RolloutPlanner.plan(3, "0", "10%", 3),
                "that one unavailable pod leaves 2 of 3 serving, below a floor of 3; throw UnsafeRolloutException");
        RolloutPlan surgeOne = RolloutPlanner.plan(3, "10%", "0", 3);
        assertEquals(1, surgeOne.surge(), "maxSurge 10% of 3 rounds UP to 1");
        assertEquals(0, surgeOne.unavailable(), "surge is 1, so unavailable stays 0 and all 3 pods keep serving");
    }

    @Test
    @DisplayName("Silver: Friday's plan, maxUnavailable 100%, is refused because it drops below the safety floor")
    void fridaysPlanIsRefused() {
        assertThrows(UnsafeRolloutException.class, () -> RolloutPlanner.plan(30, "25%", "100%", 24),
                "100% unavailable lets all 30 pods stop at once, and Spoke needs 24 serving; throw UnsafeRolloutException");
        assertThrows(IllegalArgumentException.class, () -> RolloutPlanner.plan(30, "25%", "150%", 0),
                "maxUnavailable cannot be above 100%; throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> RolloutPlanner.plan(30, "25%", "0", 31),
                "minAvailable cannot be more than replicas; throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> RolloutPlanner.plan(0, "25%", "0", 0),
                "replicas must be at least 1; throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Silver: the safety floor is checked exactly, after rounding")
    void floorBoundary() {
        RolloutPlan six = RolloutPlanner.plan(30, "25%", "6", 24);
        assertEquals(24, six.lowestServing(), "6 of 30 may stop, so exactly 24 serve at the lowest point: that is allowed");
        assertThrows(UnsafeRolloutException.class, () -> RolloutPlanner.plan(30, "25%", "7", 24),
                "7 of 30 may stop, leaving 23, which is below the floor of 24; throw UnsafeRolloutException");
        RolloutPlan ten = RolloutPlanner.plan(10, "25%", "25%", 8);
        assertEquals(8, ten.lowestServing(),
                "25% of 10 rounds DOWN to 2 unavailable, so 8 serve; rounding up to 3 would wrongly break the floor");
    }

    @Test
    @DisplayName("Silver: for many settings, every wave keeps both limits and the rollout finishes")
    void invariantsHold() {
        String[][] settings = {{"25%", "25%"}, {"1", "0"}, {"0", "1"}, {"50%", "10%"}, {"100%", "0"}, {"0", "100%"}, {"2", "3"}, {"0", "10%"}};
        for (int replicas = 1; replicas <= 12; replicas++) {
            for (String[] s : settings) {
                int pods = replicas;
                int surge = RolloutPlanner.resolve(s[0], replicas, true);
                int unavailable = Math.min(replicas, RolloutPlanner.resolve(s[1], replicas, false));
                if (surge == 0 && unavailable == 0) {
                    unavailable = 1;
                }
                String what = replicas + " pods, maxSurge " + s[0] + ", maxUnavailable " + s[1];
                RolloutPlan plan = assertDoesNotThrow(() -> RolloutPlanner.plan(pods, s[0], s[1], 0),
                        what + ": a valid rollout must plan without an exception");
                List<Wave> waves = plan.waves();
                assertTrue(!waves.isEmpty() && waves.size() <= 2 * replicas + 2,
                        what + ": the plan should finish in a sensible number of waves, but had " + waves.size());
                for (int i = 0; i < waves.size(); i++) {
                    Wave w = waves.get(i);
                    assertEquals(i + 1, w.number(), what + ": waves are numbered 1, 2, 3, ...");
                    assertTrue(w.serving() >= replicas - unavailable,
                            what + ": wave " + w.number() + " serves " + w.serving() + ", below replicas - maxUnavailable = " + (replicas - unavailable));
                    assertTrue(w.total() <= replicas + surge,
                            what + ": wave " + w.number() + " runs " + w.total() + " pods, above replicas + maxSurge = " + (replicas + surge));
                    assertTrue(w.started() + w.stopped() > 0, what + ": every wave must start or stop at least one pod");
                }
                Wave last = waves.get(waves.size() - 1);
                assertEquals(0, last.oldPods(), what + ": after the last wave no old pod is left");
                assertEquals(replicas, last.newPods(), what + ": after the last wave every pod is new");
            }
        }
    }
}
