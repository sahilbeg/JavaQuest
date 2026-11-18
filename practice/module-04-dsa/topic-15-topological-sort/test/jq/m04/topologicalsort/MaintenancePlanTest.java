package jq.m04.topologicalsort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

class MaintenancePlanTest {

    private static <T> T quickly(ThrowingSupplier<T> work) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), work,
                "This took more than 2 seconds. Check that every loop moves on and ends");
    }

    private static MaintenancePlan service() {
        MaintenancePlan plan = new MaintenancePlan();
        plan.mustComeBefore("fix brakes", "road test");
        plan.mustComeBefore("lift bike", "fix brakes");
        plan.mustComeBefore("lift bike", "oil chain");
        plan.mustComeBefore("oil chain", "road test");
        return plan;
    }

    @Test
    @DisplayName("waitingOn counts how many different jobs must come before a job")
    void waitingOnIsTheInDegree() {
        MaintenancePlan plan = new MaintenancePlan();
        plan.mustComeBefore("lift bike", "road test");
        plan.mustComeBefore("oil chain", "road test");
        plan.mustComeBefore("oil chain", "road test");
        assertEquals(2, plan.waitingOn("road test"),
                "road test waits on lift bike and oil chain. The same rule added twice still counts once, so expect 2");
        assertEquals(0, plan.waitingOn("lift bike"),
                "Nothing has to happen before lift bike, so waitingOn(\"lift bike\") should be 0");
        assertEquals(0, plan.waitingOn("paint frame"),
                "paint frame is not in the plan, so waitingOn(\"paint frame\") should be 0");
    }

    @Test
    @DisplayName("order puts every job after all the jobs that must come before it")
    void orderRespectsEveryRule() {
        List<String> order = quickly(() -> service().order());
        assertEquals(4, order.size(), "order() should list all 4 jobs exactly once, but it gave " + order);
        assertTrue(order.indexOf("lift bike") < order.indexOf("fix brakes"),
                "lift bike must come before fix brakes, but order() gave " + order);
        assertTrue(order.indexOf("lift bike") < order.indexOf("oil chain"),
                "lift bike must come before oil chain, but order() gave " + order);
        assertTrue(order.indexOf("fix brakes") < order.indexOf("road test"),
                "fix brakes must come before road test, but order() gave " + order);
        assertTrue(order.indexOf("oil chain") < order.indexOf("road test"),
                "oil chain must come before road test, but order() gave " + order);
        assertEquals(List.of("lift bike", "fix brakes", "oil chain", "road test"), order,
                "With the A-to-Z tie rule the order should be [lift bike, fix brakes, oil chain, road test]");
    }

    @Test
    @DisplayName("When several jobs are ready, the one that comes first A to Z goes next")
    void tiesGoAlphabetically() {
        MaintenancePlan plan = new MaintenancePlan();
        plan.mustComeBefore("charge battery", "check lights");
        plan.addJob("wash frame");
        plan.mustComeBefore("check lights", "road test");
        List<String> order = quickly(plan::order);
        assertEquals(List.of("charge battery", "check lights", "road test", "wash frame"), order,
                "After charge battery, both check lights and wash frame are ready. check lights comes first A to Z, "
                        + "so it goes next. A plain first-in-first-out queue would pick wash frame. Use a PriorityQueue");
    }

    @Test
    @DisplayName("order includes jobs with no rules, lists each job once, and is empty for an empty plan")
    void loneJobsAndEmptyPlan() {
        MaintenancePlan plan = new MaintenancePlan();
        plan.addJob("wash frame");
        plan.addJob("wash frame");
        plan.mustComeBefore("lift bike", "oil chain");
        assertEquals(List.of("lift bike", "oil chain", "wash frame"), quickly(plan::order),
                "wash frame has no rules but is still a job. Adding it twice keeps one copy, so expect [lift bike, oil chain, wash frame]");
        assertEquals(List.of(), quickly(() -> new MaintenancePlan().order()),
                "A plan with no jobs should give an empty order");
    }

    @Test
    @DisplayName("order throws IllegalStateException when the rules make a cycle")
    void cycleThrows() {
        MaintenancePlan plan = new MaintenancePlan();
        plan.mustComeBefore("order parts", "fix brakes");
        plan.mustComeBefore("fix brakes", "road test");
        plan.mustComeBefore("road test", "order parts");
        assertThrows(IllegalStateException.class, () -> quickly(plan::order),
                "order parts -> fix brakes -> road test -> order parts is a cycle, so no order exists. "
                        + "order() should throw IllegalStateException");
    }

    @Test
    @DisplayName("A cycle in one corner still means no order, even when other jobs are fine")
    void cycleBesideGoodJobs() {
        MaintenancePlan plan = new MaintenancePlan();
        plan.mustComeBefore("lift bike", "oil chain");
        plan.mustComeBefore("pump tyres", "road test");
        plan.mustComeBefore("road test", "pump tyres");
        assertThrows(IllegalStateException.class, () -> quickly(plan::order),
                "lift bike and oil chain can be ordered, but pump tyres and road test wait on each other. "
                        + "Kahn's result has only 2 of the 4 jobs: when it is short, throw IllegalStateException");
    }

    @Test
    @DisplayName("A job that must come before itself is a cycle")
    void selfRuleIsACycle() {
        MaintenancePlan plan = new MaintenancePlan();
        plan.mustComeBefore("road test", "road test");
        assertThrows(IllegalStateException.class, () -> quickly(plan::order),
                "road test can never be ready if it waits on itself, so order() should throw IllegalStateException");
    }

    @Test
    @DisplayName("hasCycle says whether the rules contain a cycle")
    void hasCycleAnswers() {
        MaintenancePlan loop = new MaintenancePlan();
        loop.mustComeBefore("pump tyres", "road test");
        loop.mustComeBefore("road test", "pump tyres");
        assertTrue(quickly(loop::hasCycle),
                "pump tyres and road test each wait on the other, so hasCycle() should be true");
        assertFalse(quickly(() -> service().hasCycle()),
                "The service plan has no cycle, so hasCycle() should be false");
    }
}
