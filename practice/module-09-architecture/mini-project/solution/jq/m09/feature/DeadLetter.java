package jq.m09.feature;

/** An event billing gave up on, with the reason, for a person to look at. Ready to use: do not change. */
public record DeadLetter(RideEnded event, String reason) {
}
