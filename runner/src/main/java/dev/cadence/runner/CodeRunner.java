package dev.cadence.runner;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Runs a ticket's code: its checks (tests), its program, and lesson snippets. {@link PracticeRunner} does it for Java
 * with javac and JUnit; {@link NodeRunner} does it for JavaScript and React with Node and Vitest. Both report in the
 * same shapes, so the app, the progress rules and the pages don't care which one ran.
 */
public interface CodeRunner {

    RunLimits limits();

    TestRunResult run(Path mainSources, Path testSources, RunLimits limits) throws IOException, InterruptedException;

    /** Runs the program that starts at {@code entry} (a Java class or a JavaScript file), feeding it {@code input}. */
    ProgramResult runMain(Path sources, String entry, RunLimits limits, String input) throws IOException, InterruptedException;

    ProgramResult runSnippet(String code, String input) throws IOException, InterruptedException;
}
