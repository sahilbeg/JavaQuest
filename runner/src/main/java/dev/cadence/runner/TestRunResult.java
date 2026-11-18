package dev.cadence.runner;

import java.time.Duration;
import java.util.List;

public record TestRunResult(
        Outcome outcome,
        List<TestCaseResult> tests,
        List<CompileError> compileErrors,
        String output,
        Duration duration) {

    public enum Outcome { PASSED, FAILED, COMPILE_ERROR, TIMED_OUT, CRASHED }

    public record TestCaseResult(String className, String name, boolean passed, String message) {}

    public record CompileError(String file, long line, String message) {}

    public TestRunResult {
        tests = List.copyOf(tests);
        compileErrors = List.copyOf(compileErrors);
    }

    public long passedCount() {
        return tests.stream().filter(TestCaseResult::passed).count();
    }
}
