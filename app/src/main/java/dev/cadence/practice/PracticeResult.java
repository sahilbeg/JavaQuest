package dev.cadence.practice;

import dev.cadence.runner.TestRunResult;
import java.util.List;

public record PracticeResult(String topicId, Source source, TestRunResult result, List<String> explanations) {

    public enum Source { BROWSER, IDE }

    public PracticeResult(String topicId, Source source, TestRunResult result) {
        this(topicId, source, result, CompilerHelp.explainAll(result.compileErrors()));
    }
}
