package jq.m12.pipelines;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * One task in a DAG: its name, the tasks that must succeed first (upstream),
 * how many times to retry it, how long to wait between tries, and its action.
 */
public record TaskSpec(String name, List<String> upstream, int maxRetries, Duration retryDelay, TaskAction action) {

    public TaskSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("a task needs a name");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries cannot be negative: " + maxRetries);
        }
        Objects.requireNonNull(retryDelay, "retryDelay");
        Objects.requireNonNull(action, "action");
        upstream = List.copyOf(upstream);
    }

    /** A task with no retries: TaskSpec.of("load_report", action, "extract_rides"). */
    public static TaskSpec of(String name, TaskAction action, String... upstream) {
        return new TaskSpec(name, List.of(upstream), 0, Duration.ZERO, action);
    }

    /** The same task, retried up to maxRetries times, waiting retryDelay before each retry. */
    public TaskSpec withRetries(int maxRetries, Duration retryDelay) {
        return new TaskSpec(name, upstream, maxRetries, retryDelay, action);
    }
}
