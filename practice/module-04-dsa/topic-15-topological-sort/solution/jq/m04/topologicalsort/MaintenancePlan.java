package jq.m04.topologicalsort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MaintenancePlan {

    // For each job, the jobs that must wait for it: an arrow first -> then.
    private final Map<String, Set<String>> after = new TreeMap<>();

    public void addJob(String job) {
        after.computeIfAbsent(job, k -> new TreeSet<>());
    }

    public void mustComeBefore(String first, String then) {
        addJob(first);
        addJob(then);
        after.get(first).add(then);
    }

    public int waitingOn(String job) {
        int count = 0;
        for (Set<String> next : after.values()) {
            if (next.contains(job)) {
                count++;
            }
        }
        return count;
    }

    public List<String> order() {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String job : after.keySet()) {
            inDegree.putIfAbsent(job, 0);
            for (String next : after.get(job)) {
                inDegree.merge(next, 1, Integer::sum);
            }
        }
        PriorityQueue<String> ready = new PriorityQueue<>();
        for (String job : after.keySet()) {
            if (inDegree.get(job) == 0) {
                ready.offer(job);
            }
        }
        List<String> result = new ArrayList<>();
        while (!ready.isEmpty()) {
            String job = ready.poll();
            result.add(job);
            for (String next : after.get(job)) {
                int left = inDegree.get(next) - 1;
                inDegree.put(next, left);
                if (left == 0) {
                    ready.offer(next);
                }
            }
        }
        if (result.size() < after.size()) {
            throw new IllegalStateException("cycle");
        }
        return result;
    }

    public boolean hasCycle() {
        try {
            order();
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public static void main(String[] args) {
        MaintenancePlan plan = new MaintenancePlan();
        plan.mustComeBefore("lift bike", "fix brakes");
        plan.mustComeBefore("lift bike", "oil chain");
        plan.mustComeBefore("fix brakes", "test ride");
        plan.mustComeBefore("oil chain", "test ride");
        plan.addJob("clean seat");
        System.out.println(plan.order());
    }
}
