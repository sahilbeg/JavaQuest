package jq.m09.hexagonal;

/**
 * PayRiver's request format. amount is a decimal string with exactly two digits after a dot, like "4.60".
 * customer is our rider id and reference is our rental id.
 */
public record PayRiverChargeRequest(String customer, String amount, String currency, String reference) {
}
