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
        // TODO 7: call runOne for every task name in dag.order(), then return succeeded(logicalDate)
        return false;
    }

    /** Runs each date from 'from' to 'to', both included, oldest first, skipping dates that already fully succeeded. */
    public List<LocalDate> backfill(LocalDate from, LocalDate to) {
        // TODO 8: throw IllegalArgumentException when from is after to.
        //         For each date from 'from' to 'to': if it has not fully succeeded, run it and add it to the list.
        return new ArrayList<>();
    }

    /** Runs again only the FAILED and UPSTREAM_FAILED tasks of a date, in dag.order(). */
    public boolean rerunFailed(LocalDate logicalDate) {
        // TODO 9: throw IllegalStateException when the DAG never ran for this date (states has no entry for it).
        //         Call runOne, in dag.order(), for each task whose state is FAILED or UPSTREAM_FAILED. Return succeeded.
        return false;
    }

    private void runOne(String name, LocalDate date) {
        TaskSpec task = dag.task(name);
        // TODO 10: if any upstream task is not SUCCESS for this date, record UPSTREAM_FAILED and return without calling it
        // TODO 11: call task.action().run(date), counting every attempt in 'attempts'. Success: record SUCCESS.
        //          On an Exception: after 1 + task.maxRetries() attempts, record FAILED. Otherwise add
        //          new RetryWait(name, date, task.retryDelay()) to waits, and try again. Never sleep.
    }

    private void record(String name, LocalDate date, TaskState state) {
        states.computeIfAbsent(date, d -> new HashMap<>()).put(name, state);
    }

    /** True when every task of the DAG is SUCCESS for this date. */
    public boolean succeeded(LocalDate logicalDate) {
        // TODO 12: true only when every task in dag.order() is SUCCESS for this date
        return false;
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
