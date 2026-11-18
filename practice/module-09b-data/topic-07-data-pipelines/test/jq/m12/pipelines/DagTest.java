package jq.m12.pipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DagTest {

    private static final TaskAction NOTHING = date -> { };
    private static final Duration TWO_SECONDS = Duration.ofSeconds(2);

    private static Dag dag(TaskSpec... tasks) {
        return assertTimeoutPreemptively(TWO_SECONDS, () -> new Dag("daily_ride_report", List.of(tasks)),
                "making the DAG took over 2 seconds: does your ordering loop ever stop?");
    }

    private static InvalidDagException invalid(String why, TaskSpec... tasks) {
        return assertTimeoutPreemptively(TWO_SECONDS,
                () -> assertThrows(InvalidDagException.class, () -> new Dag("daily_ride_report", List.of(tasks)), why),
                "checking the DAG took over 2 seconds: does your ordering loop ever stop?");
    }

    @Test
    @DisplayName("A chain of three tasks runs in dependency order, whatever order they were listed in")
    void chainRunsInDependencyOrder() {
        Dag dag = dag(
                TaskSpec.of("load_report", NOTHING, "clean_rides"),
                TaskSpec.of("extract_rides", NOTHING),
                TaskSpec.of("clean_rides", NOTHING, "extract_rides"));

        assertEquals(List.of("extract_rides", "clean_rides", "load_report"), dag.order(),
                "load_report needs clean_rides, which needs extract_rides, so extract_rides must come first");
    }

    @Test
    @DisplayName("When several tasks are ready, the alphabetically first one goes next")
    void tiesAreBrokenAlphabetically() {
        Dag dag = dag(
                TaskSpec.of("publish_report", NOTHING, "sum_fares", "count_rides"),
                TaskSpec.of("sum_fares", NOTHING, "extract_rides"),
                TaskSpec.of("count_rides", NOTHING, "extract_rides"),
                TaskSpec.of("extract_rides", NOTHING),
                TaskSpec.of("archive_logs", NOTHING));

        assertEquals(List.of("archive_logs", "extract_rides", "count_rides", "sum_fares", "publish_report"), dag.order(),
                "archive_logs and extract_rides are both ready at the start, and archive_logs comes first alphabetically."
                        + " Then count_rides and sum_fares are both ready: count_rides comes first");
    }

    @Test
    @DisplayName("A task that becomes ready can jump ahead of a task that was ready earlier, if its name comes first")
    void newlyReadyTaskCanJumpTheQueue() {
        Dag dag = dag(
                TaskSpec.of("b_extract", NOTHING),
                TaskSpec.of("z_vacuum", NOTHING),
                TaskSpec.of("a_clean", NOTHING, "b_extract"));

        assertEquals(List.of("b_extract", "a_clean", "z_vacuum"), dag.order(),
                "after b_extract runs, the ready tasks are a_clean and z_vacuum. Always take the smallest name among ALL"
                        + " ready tasks (a PriorityQueue does this), not the tasks in the order they became ready");
    }

    @Test
    @DisplayName("A dependency on a task that does not exist is refused, naming both tasks")
    void unknownDependencyIsRefused() {
        InvalidDagException e = invalid("load_report depends on extract_ride, which is not in the DAG: throw InvalidDagException",
                TaskSpec.of("extract_rides", NOTHING),
                TaskSpec.of("load_report", NOTHING, "extract_ride"));

        assertTrue(e.getMessage().contains("load_report") && e.getMessage().contains("extract_ride"),
                "the message should name the task (load_report) and the unknown dependency (extract_ride), but was: " + e.getMessage());
    }

    @Test
    @DisplayName("A cycle is refused, and the message names the tasks that can never start")
    void cycleIsRefused() {
        InvalidDagException e = invalid("clean -> load -> check -> clean is a cycle: throw InvalidDagException",
                TaskSpec.of("extract_rides", NOTHING),
                TaskSpec.of("clean_rides", NOTHING, "extract_rides", "check_totals"),
                TaskSpec.of("load_report", NOTHING, "clean_rides"),
                TaskSpec.of("check_totals", NOTHING, "load_report"));

        String message = e.getMessage();
        assertTrue(message.contains("check_totals") && message.contains("clean_rides") && message.contains("load_report"),
                "the message should name every task stuck in the cycle (check_totals, clean_rides, load_report), but was: " + message);
        assertFalse(message.contains("extract_rides"),
                "extract_rides can start, so it is not part of the problem and should not be named, but the message was: " + message);
    }

    @Test
    @DisplayName("A task that depends on itself is a cycle too")
    void selfDependencyIsACycle() {
        InvalidDagException e = invalid("a task cannot wait for itself: throw InvalidDagException",
                TaskSpec.of("extract_rides", NOTHING, "extract_rides"));

        assertTrue(e.getMessage().contains("extract_rides"),
                "the message should name extract_rides, but was: " + e.getMessage());
    }

    @Test
    @DisplayName("A DAG with no tasks, or two tasks with the same name, is refused")
    void emptyAndDuplicateAreRefused() {
        invalid("a DAG with no tasks has nothing to run: throw InvalidDagException");

        InvalidDagException e = invalid("two tasks called extract_rides: throw InvalidDagException",
                TaskSpec.of("extract_rides", NOTHING),
                TaskSpec.of("extract_rides", NOTHING));
        assertTrue(e.getMessage().contains("extract_rides"),
                "the message should name the duplicate, extract_rides, but was: " + e.getMessage());
    }
}
