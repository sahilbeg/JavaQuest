package dev.javaquest.practice;

import dev.javaquest.runner.ProgramResult;
import java.util.List;

public record ProgramRun(ProgramResult result, List<String> explanations) {

    public ProgramRun(ProgramResult result) {
        this(result, CompilerHelp.explainAll(result.compileErrors()));
    }
}
