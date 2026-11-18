package jq.m11.incident;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plans a rolling update the way Spoke's deploy tool runs it: in waves, never with fewer than
 * minAvailable pods serving riders.
 *
 * This is the code that ran on Friday.
 */
public final class RolloutPlanner {

    private static final Pattern COUNT = Pattern.compile("\\d+");
    private static final Pattern PERCENT = Pattern.compile("(\\d+)%");

    private RolloutPlanner() {
    }

    /**
     * Ready to use, as in the Kubernetes manifests ticket. Turns a maxSurge or maxUnavailable value into a number
     * of pods. "3" is 3 pods. "25%" is 25% of replicas, rounded up when roundUp is true (maxSurge) and down when
     * it is false (maxUnavailable). Anything else throws IllegalArgumentException.
     */
    public static int resolve(String value, int replicas, boolean roundUp) {
        if (value == null) {
            throw new IllegalArgumentException("a rollout setting is missing");
        }
        if (COUNT.matcher(value).matches()) {
            return Integer.parseInt(value);
        }
        Matcher percent = PERCENT.matcher(value);
        if (percent.matches()) {
            long scaled = (long) replicas * Integer.parseInt(percent.group(1));
            return (int) (roundUp ? (scaled + 99) / 100 : scaled / 100);
        }
        throw new IllegalArgumentException("a rollout setting is a whole number like 2 or a percentage like 25%, not " + value);
    }

    public static RolloutPlan plan(int replicas, String maxSurge, String maxUnavailable, int minAvailable) {
        int surge = resolve(maxSurge, replicas, true);
        int unavailable = resolve(maxUnavailable, replicas, false);
        // TODO 3 (silver): check the input in the README's order. IllegalArgumentException for replicas below 1,
        // minAvailable outside 0..replicas, maxSurge and maxUnavailable both written as 0, and a maxUnavailable
        // percentage above 100%. When rounding makes surge and unavailable both 0, allow 1 unavailable pod, as
        // Kubernetes does. Cap unavailable at replicas. Throw UnsafeRolloutException when
        // replicas - unavailable < minAvailable.

        List<Wave> waves = new ArrayList<>();
        int oldPods = replicas;
        int newPods = 0;
        while (oldPods > 0 || newPods < replicas) {
            // TODO 4 (silver): work out stopped and started with the README's formulas, so that no wave serves
            // fewer than replicas - unavailable pods and no wave runs more than replicas + surge.
            // Friday's version: stop every old pod and start every new one, all in one wave.
            int stopped = oldPods;
            int started = replicas - newPods;
            int serving = oldPods - stopped + newPods;
            // Ready-made guards: a plan that cannot move, or never ends, fails with a message. Keep them.
            if (stopped == 0 && started == 0) {
                throw new IllegalStateException("the rollout makes no progress after wave " + waves.size()
                        + ": no pod can stop or start with surge " + surge + " and unavailable " + unavailable
                        + " (did you skip README step 5?)");
            }
            if (waves.size() >= 2 * replicas + 2) {
                throw new IllegalStateException("the rollout still is not finished after " + waves.size()
                        + " waves; check the wave formulas in the README");
            }
            oldPods -= stopped;
            newPods += started;
            waves.add(new Wave(waves.size() + 1, started, stopped, serving, oldPods, newPods));
        }
        return new RolloutPlan(replicas, surge, unavailable, waves);
    }
}
