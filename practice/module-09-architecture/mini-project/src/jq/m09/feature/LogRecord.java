package jq.m09.feature;

/**
 * One event stored in the log, with where it is stored. Ready to use: do not change.
 *
 * @param partition which partition holds it
 * @param offset    its position in that partition: 0 for the first record, then 1, 2, ...
 * @param key       the key it was appended with (the rider id)
 * @param event     the event itself
 */
public record LogRecord(int partition, long offset, String key, RideEnded event) {
}
