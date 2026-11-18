package jq.m11.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Plans a workflow run: which jobs can run side by side, and how many jobs a matrix makes. */
public final class JobPlanner {

    /** The jobs in waves: every job in a wave has all its needs in earlier waves. File order inside a wave. */
    public List<List<String>> waves(String yamlText) {
        Map<?, ?> jobs = (Map<?, ?>) Workflows.load(yamlText).get("jobs");
        Map<String, List<String>> needs = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : jobs.entrySet()) {
            String name = String.valueOf(entry.getKey());
            Object job = entry.getValue();
            needs.put(name, needsOf(name, job instanceof Map<?, ?> map ? map.get("needs") : null));
        }

        Map<String, Integer> waiting = new HashMap<>();          // how many needs are not finished yet
        Map<String, List<String>> unlocks = new HashMap<>();      // need -> the jobs waiting for it
        for (Map.Entry<String, List<String>> entry : needs.entrySet()) {
            for (String need : entry.getValue()) {
                if (!needs.containsKey(need)) {
                    throw new InvalidWorkflowException("job " + entry.getKey() + " needs " + need + ", but there is no job called " + need);
                }
                unlocks.computeIfAbsent(need, k -> new ArrayList<>()).add(entry.getKey());
            }
            waiting.put(entry.getKey(), entry.getValue().size());
        }

        List<String> fileOrder = new ArrayList<>(needs.keySet());
        List<List<String>> waves = new ArrayList<>();
        List<String> wave = new ArrayList<>();
        for (String name : fileOrder) {
            if (waiting.get(name) == 0) {
                wave.add(name);
            }
        }
        int planned = 0;
        while (!wave.isEmpty()) {
            waves.add(wave);
            planned += wave.size();
            List<String> next = new ArrayList<>();
            for (String done : wave) {
                for (String job : unlocks.getOrDefault(done, List.of())) {
                    if (waiting.merge(job, -1, Integer::sum) == 0) {
                        next.add(job);
                    }
                }
            }
            next.sort((a, b) -> Integer.compare(fileOrder.indexOf(a), fileOrder.indexOf(b)));
            wave = next;
        }
        if (planned < fileOrder.size()) {
            List<String> stuck = new ArrayList<>();
            for (String name : fileOrder) {
                if (waiting.get(name) > 0) {
                    stuck.add(name);
                }
            }
            throw new InvalidWorkflowException("the needs form a cycle, so these jobs can never start: " + String.join(", ", stuck));
        }
        return waves;
    }

    private static List<String> needsOf(String job, Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof String need) {
            return List.of(need);
        }
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object need : list) {
                if (!result.contains(String.valueOf(need))) {
                    result.add(String.valueOf(need));
                }
            }
            return result;
        }
        throw new InvalidWorkflowException("needs of job " + job + " must be a job name or a list of job names");
    }

    /** How many jobs this job becomes: 1 without a matrix, otherwise its combinations after exclude and include. */
    public int jobCount(String yamlText, String jobName) {
        Map<?, ?> jobs = (Map<?, ?>) Workflows.load(yamlText).get("jobs");
        if (!(jobs.get(jobName) instanceof Map<?, ?> job)) {
            throw new InvalidWorkflowException("there is no job called " + jobName);
        }
        if (!(job.get("strategy") instanceof Map<?, ?> strategy) || !(strategy.get("matrix") instanceof Map<?, ?> matrix)) {
            return 1;
        }

        List<Map<Object, Object>> combos = new ArrayList<>();
        combos.add(new LinkedHashMap<>());
        List<Object> axes = new ArrayList<>();
        for (Map.Entry<?, ?> axis : matrix.entrySet()) {
            if (axis.getKey().equals("include") || axis.getKey().equals("exclude")) {
                continue;
            }
            if (!(axis.getValue() instanceof List<?> values) || values.isEmpty()) {
                throw new InvalidWorkflowException("matrix key " + axis.getKey() + " of job " + jobName + " must be a list of values");
            }
            axes.add(axis.getKey());
            List<Map<Object, Object>> grown = new ArrayList<>();
            for (Map<Object, Object> combo : combos) {
                for (Object value : values) {
                    Map<Object, Object> bigger = new LinkedHashMap<>(combo);
                    bigger.put(axis.getKey(), value);
                    grown.add(bigger);
                }
            }
            combos = grown;
        }
        List<?> includes = matrix.get("include") instanceof List<?> list ? list : List.of();
        if (axes.isEmpty()) {
            return includes.size();
        }

        if (matrix.get("exclude") instanceof List<?> excludes) {
            for (Object exclude : excludes) {
                if (exclude instanceof Map<?, ?> rule) {
                    combos.removeIf(combo -> rule.entrySet().stream()
                            .allMatch(e -> combo.containsKey(e.getKey()) && Objects.equals(combo.get(e.getKey()), e.getValue())));
                }
            }
        }

        int added = 0;
        for (Object include : includes) {
            if (!(include instanceof Map<?, ?> extra)) {
                continue;
            }
            boolean fitsSomeCombo = combos.stream().anyMatch(combo -> extra.entrySet().stream()
                    .filter(e -> axes.contains(e.getKey()))
                    .allMatch(e -> Objects.equals(combo.get(e.getKey()), e.getValue())));
            if (!fitsSomeCombo) {
                added++;
            }
        }
        return combos.size() + added;
    }
}
