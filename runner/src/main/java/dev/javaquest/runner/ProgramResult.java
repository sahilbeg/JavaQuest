package dev.javaquest.runner;

import dev.javaquest.runner.TestRunResult.CompileError;
import java.time.Duration;
import java.util.List;

public record ProgramResult(Status status, List<CompileError> compileErrors, String output, Duration duration) {

    public enum Status { COMPLETED, EXCEPTION, COMPILE_ERROR, TIMED_OUT }

    public ProgramResult {
        compileErrors = List.copyOf(compileErrors);
    }
}
