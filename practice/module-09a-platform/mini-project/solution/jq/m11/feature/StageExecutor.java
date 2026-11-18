package jq.m11.feature;

/**
 * Does the real work of a stage: runs Maven, pushes the image, deploys, runs the smoke checks, shifts traffic.
 * The engine decides WHEN a stage runs; the executor only knows HOW. The checks use a fake.
 */
public interface StageExecutor {

    /** Runs a BUILD, TEST, IMAGE, DEPLOY, SMOKE, CANARY or PROMOTE stage. It may also throw. */
    StageResult execute(Stage stage);

    /** Sends all traffic back to the old version and removes the canary. */
    void rollBack(Stage canary);
}
