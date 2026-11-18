package jq.m11.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Records every stage it runs. It can be told to fail a stage, or to throw. */
final class FakeExecutor implements StageExecutor {

    final List<String> executed = new ArrayList<>();
    final List<Stage> stagesSeen = new ArrayList<>();
    final List<String> rolledBack = new ArrayList<>();
    private final Map<String, Integer> failuresLeft = new HashMap<>();
    private final Map<String, RuntimeException> throwOn = new HashMap<>();

    /** The next {@code times} runs of this stage fail. */
    FakeExecutor fail(String stage, int times) {
        failuresLeft.put(stage, times);
        return this;
    }

    FakeExecutor throwOn(String stage, RuntimeException e) {
        throwOn.put(stage, e);
        return this;
    }

    int runs(String stage) {
        return (int) executed.stream().filter(stage::equals).count();
    }

    @Override
    public StageResult execute(Stage stage) {
        executed.add(stage.name());
        stagesSeen.add(stage);
        RuntimeException e = throwOn.remove(stage.name());
        if (e != null) {
            throw e;
        }
        int left = failuresLeft.getOrDefault(stage.name(), 0);
        if (left > 0) {
            failuresLeft.put(stage.name(), left - 1);
            return StageResult.failed(stage.name() + " failed: 3 tests red");
        }
        return StageResult.ok(stage.name() + " done");
    }

    @Override
    public void rollBack(Stage canary) {
        rolledBack.add(canary.name());
    }
}
