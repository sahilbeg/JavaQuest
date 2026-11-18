package jq.m11.manifests;

/**
 * The limits of one rolling update, after percentages are turned into Pod counts. Given to you, finished.
 *
 * @param maxSurge       extra Pods allowed above replicas
 * @param maxUnavailable Pods allowed to be missing below replicas
 * @param maxPods        replicas + maxSurge: the most Pods that exist at any moment
 * @param minAvailable   replicas - maxUnavailable: the fewest ready Pods at any moment
 */
public record RolloutLimits(int maxSurge, int maxUnavailable, int maxPods, int minAvailable) {
}
