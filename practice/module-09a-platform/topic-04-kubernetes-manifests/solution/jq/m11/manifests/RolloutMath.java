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
        if (value == null) {
            throw new IllegalArgumentException("a value is needed, such as 1 or 25%");
        }
        String text = value.trim();
        boolean percent = text.endsWith("%");
        String digits = percent ? text.substring(0, text.length() - 1) : text;
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit) || digits.length() > 9) {
            throw new IllegalArgumentException("not a whole number or a percentage: " + value);
        }
        int number = Integer.parseInt(digits);
        if (!percent) {
            return number;
        }
        long scaled = (long) number * replicas;
        return (int) (roundUp ? (scaled + 99) / 100 : scaled / 100);
    }

    /** The limits of a rollout of {@code replicas} Pods with the given maxSurge and maxUnavailable. */
    public static RolloutLimits limits(int replicas, String maxSurge, String maxUnavailable) {
        if (replicas < 1) {
            throw new IllegalArgumentException("replicas must be at least 1");
        }
        if (rawNumber(maxSurge) == 0 && rawNumber(maxUnavailable) == 0) {
            throw new IllegalArgumentException("maxSurge and maxUnavailable cannot both be 0: no Pod could ever be replaced");
        }
        if (maxUnavailable.trim().endsWith("%") && rawNumber(maxUnavailable) > 100) {
            throw new IllegalArgumentException("maxUnavailable cannot be more than 100%");
        }
        int surge = resolve(maxSurge, replicas, true);
        int unavailable = resolve(maxUnavailable, replicas, false);
        if (surge == 0 && unavailable == 0) {
            unavailable = 1;   // rounding made both 0: Kubernetes allows one unavailable Pod
        }
        unavailable = Math.min(unavailable, replicas);
        return new RolloutLimits(surge, unavailable, replicas + surge, replicas - unavailable);
    }

    /** Every state of the rollout, from (replicas, 0) to (0, replicas), assuming new Pods are ready at once. */
    public static List<PodCounts> plan(int replicas, String maxSurge, String maxUnavailable) {
        RolloutLimits limits = limits(replicas, maxSurge, maxUnavailable);
        int oldPods = replicas;
        int newPods = 0;
        List<PodCounts> plan = new ArrayList<>();
        plan.add(new PodCounts(oldPods, newPods));
        while (oldPods > 0 || newPods < replicas) {
            int add = Math.min(replicas - newPods, limits.maxPods() - (oldPods + newPods));
            if (add > 0) {
                newPods += add;
                plan.add(new PodCounts(oldPods, newPods));
            }
            int remove = Math.min(oldPods, (oldPods + newPods) - limits.minAvailable());
            if (remove > 0) {
                oldPods -= remove;
                plan.add(new PodCounts(oldPods, newPods));
            }
        }
        return plan;
    }

    private static int rawNumber(String value) {
        return resolve(value, 100, false);   // the number written, with or without its % sign
    }
}
