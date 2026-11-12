package dev.javaquest.practice;

import dev.javaquest.runner.TestRunResult;

public record PracticeResult(String topicId, Source source, TestRunResult result) {

    public enum Source { BROWSER, IDE }
}
