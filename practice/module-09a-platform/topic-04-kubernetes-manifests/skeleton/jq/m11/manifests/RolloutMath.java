package jq.m11.manifests;

import java.util.ArrayList;
import java.util.List;

/** The arithmetic of a Deployment's RollingUpdate strategy, with Kubernetes' own rounding rules. */
public final class RolloutMath {

    private RolloutMath() {
    }

    /**
     * Turns "1" or "25%" into a number of Pods. A percentage of replicas is rounded up when
     * {@code roundUp} is true (maxSurge) and down when it is false (maxUnavailable).
     */
    public static int resolve(String value, int replicas, boolean roundUp) {
        // TODO 8: IllegalArgumentException for null, empty, negative or non-numeric text (trim it first).
        //         "4" is 4. "25%" is 25 * replicas / 100, rounded up or down as roundUp says. Use whole-number maths.
        return 0;
    }

    /** The limits of a rollout of {@code replicas} Pods with the given maxSurge and maxUnavailable. */
    public static RolloutLimits limits(int replicas, String maxSurge, String maxUnavailable) {
        // TODO 9: IllegalArgumentException when replicas < 1, when both values are written as 0 (0 or 0%),
        //         or when maxUnavailable is a percentage above 100%.
        //         surge = resolve(maxSurge, replicas, true); unavailable = resolve(maxUnavailable, replicas, false).
        //         If both came out 0, unavailable becomes 1. Then cap unavailable at replicas.
        //         Return new RolloutLimits(surge, unavailable, replicas + surge, replicas - unavailable).
        return new RolloutLimits(0, 0, 0, 0);
    }

    /** Every state of the rollout, from (replicas, 0) to (0, replicas), assuming new Pods are ready at once. */
    public static List<PodCounts> plan(int replicas, String maxSurge, String maxUnavailable) {
        // TODO 10: start the list with (replicas, 0). Until it reaches (0, replicas), repeat:
        //          add = min(replicas - new, maxPods - total); if add > 0, add them and record the state.
        //          remove = min(old, total - minAvailable); if remove > 0, remove them and record the state.
        List<PodCounts> plan = new ArrayList<>();
        return plan;
    }
}
