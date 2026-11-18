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
        // TODO 1: give job an empty TreeSet in "after" if it has none yet.
        //         Adding a job twice changes nothing.
    }

    public void mustComeBefore(String first, String then) {
        // TODO 2: add both jobs, then add "then" to the set of jobs after "first".
        //         One direction only: this arrow points from first to then.
    }

    public int waitingOn(String job) {
        // TODO 3: return the in-degree of job: how many different jobs must come before it.
        return -1;
    }

    public List<String> order() {
        // TODO 4: Kahn's algorithm. Count every job's in-degree. Put each job with
        //         in-degree 0 into a PriorityQueue<String>, so the A-to-Z first ready job comes out first.
        //         Poll a job, add it to the result, and lower the in-degree of each job after it.
        //         A job whose in-degree reaches 0 joins the queue.
        //         If the result is shorter than the number of jobs, throw new IllegalStateException("cycle").
        return new ArrayList<>();
    }

    public boolean hasCycle() {
        // TODO 5: return true when the rules contain a cycle, so no order exists.
        //         You may call order() and catch the IllegalStateException.
        return false;
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
