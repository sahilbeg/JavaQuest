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
        if (taskList.isEmpty()) {
            throw new InvalidDagException("DAG " + id + " has no tasks");
        }
        for (TaskSpec task : taskList) {
            if (tasks.putIfAbsent(task.name(), task) != null) {
                throw new InvalidDagException("duplicate task name: " + task.name());
            }
        }
        for (TaskSpec task : taskList) {
            for (String up : task.upstream()) {
                if (!tasks.containsKey(up)) {
                    throw new InvalidDagException("task " + task.name() + " depends on unknown task " + up);
                }
            }
        }
        this.order = Collections.unmodifiableList(topologicalOrder());
    }

    /** Kahn's algorithm. Among ready tasks, the alphabetically first one goes next. */
    private List<String> topologicalOrder() {
        Map<String, Integer> waitingFor = new HashMap<>();
        Map<String, List<String>> downstream = new HashMap<>();
        for (TaskSpec task : tasks.values()) {
            waitingFor.put(task.name(), task.upstream().size());
            for (String up : task.upstream()) {
                downstream.computeIfAbsent(up, k -> new ArrayList<>()).add(task.name());
            }
        }
        PriorityQueue<String> ready = new PriorityQueue<>();
        waitingFor.forEach((name, count) -> {
            if (count == 0) {
                ready.add(name);
            }
        });
        List<String> result = new ArrayList<>();
        while (!ready.isEmpty()) {
            String name = ready.poll();
            result.add(name);
            for (String next : downstream.getOrDefault(name, List.of())) {
                if (waitingFor.merge(next, -1, Integer::sum) == 0) {
                    ready.add(next);
                }
            }
        }
        if (result.size() < tasks.size()) {
            TreeSet<String> stuck = new TreeSet<>(tasks.keySet());
            stuck.removeAll(result);
            throw new InvalidDagException("DAG " + id + " has a cycle: these tasks can never start " + stuck);
        }
        return result;
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
