package jq.m11.feature;

import java.time.Instant;

/** One line of a run's audit log: when, which stage, what was decided, and why. */
public record AuditEntry(Instant at, String stage, Decision decision, String detail) {
}
