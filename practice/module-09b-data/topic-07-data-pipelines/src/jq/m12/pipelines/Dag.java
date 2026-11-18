package jq.m12.pipelines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

/**
 * A DAG: tasks and the order they must run in. It is checked once, when it is made,
 * so a broken DAG fails at once, not at 2 a.m.
 */
public final class Dag {

    private final String id;
    private final Map<String, TaskSpec> tasks = new LinkedHashMap<>();
    private final List<String> order;

    public Dag(String id, List<TaskSpec> taskList) {
        this.id = id;
        // TODO 1: throw InvalidDagException when taskList is empty
        for (TaskSpec task : taskList) {
            tasks.put(task.name(), task);
            // TODO 2: throw InvalidDagException naming the task when two tasks share a name (putIfAbsent helps)
        }
        // TODO 3: throw InvalidDagException naming both tasks when an upstream name is not a task of this DAG
        this.order = Collections.unmodifiableList(topologicalOrder());
    }

    /** Kahn's algorithm. Among ready tasks, the alphabetically first one goes next. */
    private List<String> topologicalOrder() {
        // TODO 4: count each task's upstream tasks, and list each task's downstream tasks
        // TODO 5: put every task with no upstream into a PriorityQueue<String>; while it is not empty,
        //         poll the smallest name, add it to the result, and add each downstream task whose count reaches 0
        // TODO 6: if some tasks never got into the result, there is a cycle: throw InvalidDagException
        //         naming those tasks (sorted, for example with a TreeSet)
        return new ArrayList<>();
    }

    public String id() {
        return id;
    }

    /** Every task name, in the order the scheduler runs them. */
    public List<String> order() {
        return order;
    }

    /** The task with this name. */
    public TaskSpec task(String name) {
        TaskSpec task = tasks.get(name);
        if (task == null) {
            throw new IllegalArgumentException("DAG " + id + " has no task " + name);
        }
        return task;
    }
}
