package dev.cadence.practice;

import dev.cadence.runner.ProgramResult;
import java.util.List;

public record ProgramRun(ProgramResult result, List<String> explanations) {

    public ProgramRun(ProgramResult result) {
        this(result, CompilerHelp.explainAll(result.compileErrors()));
    }
}
