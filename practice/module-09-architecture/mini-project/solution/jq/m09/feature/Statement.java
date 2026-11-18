package jq.m09.feature;

/**
 * What the billing page shows for one rider. Ready to use: do not change.
 *
 * @param totalCents the sum of every successful charge
 * @param rides      how many rides were charged
 */
public record Statement(String riderId, long totalCents, int rides) {
}
