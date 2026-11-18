package jq.m11.incident;

import java.util.List;

/**
 * A rollout plan: the resolved limits and the waves that replace every old pod with a new one.
 *
 * @param replicas    pods the Deployment wants
 * @param surge       maxSurge as a number of pods
 * @param unavailable maxUnavailable as a number of pods
 * @param waves       the waves, in order
 */
public record RolloutPlan(int replicas, int surge, int unavailable, List<Wave> waves) {

    public RolloutPlan {
        waves = List.copyOf(waves);
    }

    /** The fewest pods serving riders in any wave. */
    public int lowestServing() {
        return waves.stream().mapToInt(Wave::serving).min().orElse(replicas);
    }

    /** The most pods running at once at the end of any wave. */
    public int highestTotal() {
        return waves.stream().mapToInt(Wave::total).max().orElse(replicas);
    }
}
