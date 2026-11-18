package jq.m09.hexagonal;

/** PayRiver's answer. status is "ok", "declined", or some other text when something went wrong. */
public record PayRiverResponse(String status, String chargeId) {
}
