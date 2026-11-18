package jq.m11.incident;

/**
 * One wave of a rolling update.
 *
 * @param number   1 for the first wave, then 2, 3, ...
 * @param started  new pods started in this wave
 * @param stopped  old pods stopped in this wave
 * @param serving  ready pods serving riders during this wave: the old pods still running plus the new pods
 *                 that became ready in earlier waves (the pods started in this wave are not ready yet)
 * @param oldPods  old pods left after this wave
 * @param newPods  new pods after this wave, all ready
 */
public record Wave(int number, int started, int stopped, int serving, int oldPods, int newPods) {

    /** Pods running at the end of this wave, old and new together. */
    public int total() {
        return oldPods + newPods;
    }
}
