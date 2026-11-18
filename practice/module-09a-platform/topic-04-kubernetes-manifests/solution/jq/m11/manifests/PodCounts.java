package jq.m11.manifests;

/** One moment in a rollout: how many old-version and new-version Pods exist. Given to you, finished. */
public record PodCounts(int oldPods, int newPods) {

    public int total() {
        return oldPods + newPods;
    }
}
