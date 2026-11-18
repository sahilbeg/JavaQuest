package jq.m12.pipelines;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tiny Airflow: runs one DAG for a logical date, retries failing tasks,
 * skips tasks whose upstream failed, and remembers the state of every (task, date).
 */
public final class MiniScheduler {

    private final Dag dag;
    private final Map<LocalDate, Map<String, TaskState>> states = new HashMap<>();
    private final Map<LocalDate, Map<String, Integer>> attempts = new HashMap<>();
    private final List<RetryWait> waits = new ArrayList<>();

    public MiniScheduler(Dag dag) {
        this.dag = dag;
    }

    /** Runs every task of the DAG for this date, in dag.order(). True when every task ended SUCCESS. */
    public boolean run(LocalDate logicalDate) {
        for (String name : dag.order()) {
            runOne(name, logicalDate);
        }
        return succeeded(logicalDate);
    }

    /** Runs each date from 'from' to 'to', both included, oldest first, skipping dates that already fully succeeded. */
    public List<LocalDate> backfill(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("backfill from " + from + " is after to " + to);
        }
        List<LocalDate> ran = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (!succeeded(date)) {
                run(date);
                ran.add(date);
            }
        }
        return ran;
    }

    /** Runs again only the FAILED and UPSTREAM_FAILED tasks of a date, in dag.order(). */
    public boolean rerunFailed(LocalDate logicalDate) {
        if (!states.containsKey(logicalDate)) {
            throw new IllegalStateException("nothing to re-run: the DAG never ran for " + logicalDate);
        }
        for (String name : dag.order()) {
            TaskState before = state(name, logicalDate);
            if (before == TaskState.FAILED || before == TaskState.UPSTREAM_FAILED) {
                runOne(name, logicalDate);
            }
        }
        return succeeded(logicalDate);
    }

    private void runOne(String name, LocalDate date) {
        TaskSpec task = dag.task(name);
        for (String up : task.upstream()) {
            if (state(up, date) != TaskState.SUCCESS) {
                record(name, date, TaskState.UPSTREAM_FAILED);
                return;
            }
        }
        for (int attempt = 1; ; attempt++) {
            attempts.computeIfAbsent(date, d -> new HashMap<>()).merge(name, 1, Integer::sum);
            try {
                task.action().run(date);
                record(name, date, TaskState.SUCCESS);
                return;
            } catch (Exception e) {
                if (attempt > task.maxRetries()) {
                    record(name, date, TaskState.FAILED);
                    return;
                }
                waits.add(new RetryWait(name, date, task.retryDelay()));
            }
        }
    }

    private void record(String name, LocalDate date, TaskState state) {
        states.computeIfAbsent(date, d -> new HashMap<>()).put(name, state);
    }

    /** True when every task of the DAG is SUCCESS for this date. */
    public boolean succeeded(LocalDate logicalDate) {
        for (String name : dag.order()) {
            if (state(name, logicalDate) != TaskState.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    /** The state of one task for one date: NONE when it never ran for that date. */
    public TaskState state(String task, LocalDate logicalDate) {
        dag.task(task);
        return states.getOrDefault(logicalDate, Map.of()).getOrDefault(task, TaskState.NONE);
    }

    /** How many times this task's action was called for this date, over every run. */
    public int attempts(String task, LocalDate logicalDate) {
        dag.task(task);
        return attempts.getOrDefault(logicalDate, Map.of()).getOrDefault(task, 0);
    }

    /** Every wait before a retry, in the order they happened. */
    public List<RetryWait> waits() {
        return Collections.unmodifiableList(waits);
    }
}
