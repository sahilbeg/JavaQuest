package jq.m12.pipelines;

import java.time.Duration;
import java.time.LocalDate;

/** One wait before a retry: the scheduler records it instead of sleeping. */
public record RetryWait(String task, LocalDate logicalDate, Duration delay) {
}
