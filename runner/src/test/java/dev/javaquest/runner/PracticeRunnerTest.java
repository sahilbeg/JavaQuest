package dev.javaquest.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.javaquest.runner.TestRunResult.Outcome;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class PracticeRunnerTest {

    private static final Path FIXTURES = Path.of("src/test/resources/fixtures");
    private static final Path CONSOLE_JAR = Path.of(System.getProperty("junit.console.jar"));
    private static final RunLimits LIMITS = new RunLimits(Duration.ofSeconds(5), 64, 4096);

    private final PracticeRunner runner =
            new PracticeRunner(PracticeRunner.currentJavaExecutable(), List.of(CONSOLE_JAR), LIMITS);

    private TestRunResult run(String fixture) throws Exception {
        Path root = FIXTURES.resolve(fixture);
        return runner.run(root.resolve("src"), root.resolve("test"));
    }

    @Test
    void passingCodeReportsEveryTestWithItsDisplayName() throws Exception {
        TestRunResult result = run("passing");

        assertEquals(Outcome.PASSED, result.outcome());
        assertEquals("fx.AdderTest", result.tests().getFirst().className());
        assertEquals(List.of("add puts two numbers together", "add works with negative numbers"),
                result.tests().stream().map(TestRunResult.TestCaseResult::name).sorted().toList());
    }

    @Test
    void failingCodeCarriesTheAssertionMessage() throws Exception {
        TestRunResult result = run("failing");

        assertEquals(Outcome.FAILED, result.outcome());
        assertEquals(0, result.passedCount());
        assertTrue(result.tests().stream().anyMatch(t -> t.message().startsWith("add(2, 3) should be 5")),
                () -> "messages were " + result.tests());
    }

    @Test
    void compileErrorNamesTheFileAndLine() throws Exception {
        TestRunResult result = run("compile-error");

        assertEquals(Outcome.COMPILE_ERROR, result.outcome());
        var error = result.compileErrors().getFirst();
        assertEquals(Path.of("fx", "Adder.java").toString(), error.file());
        assertEquals(5, error.line());
    }

    @Test
    void infiniteLoopIsStoppedAtTheTimeLimit() throws Exception {
        TestRunResult result = run("infinite-loop");

        assertEquals(Outcome.TIMED_OUT, result.outcome());
        assertTrue(result.duration().compareTo(Duration.ofSeconds(8)) < 0, () -> "took " + result.duration());
    }

    @Test
    void aPerRunTimeLimitReplacesTheDefault() throws Exception {
        Path root = FIXTURES.resolve("infinite-loop");
        TestRunResult result = runner.run(root.resolve("src"), root.resolve("test"), Duration.ofSeconds(1));

        assertEquals(Outcome.TIMED_OUT, result.outcome());
        assertTrue(result.duration().compareTo(Duration.ofSeconds(4)) < 0, () -> "took " + result.duration());
    }

    @Test
    void perRunLimitsCanRaiseTheHeap() throws Exception {
        Path root = FIXTURES.resolve("needs-heap");

        assertEquals(Outcome.CRASHED, runner.run(root.resolve("src"), root.resolve("test")).outcome(),
                "100 MB does not fit in the default 64 MB");
        TestRunResult roomy = runner.run(root.resolve("src"), root.resolve("test"), LIMITS.withHeapMegabytes(256));
        assertEquals(Outcome.PASSED, roomy.outcome(), roomy::output);
    }

    @Test
    void systemExitCountsAsACrash() throws Exception {
        assertEquals(Outcome.CRASHED, run("exits").outcome());
    }

    @Test
    void outputIsCutOffAtTheCap() throws Exception {
        TestRunResult result = run("chatty");

        assertEquals(Outcome.PASSED, result.outcome());
        assertTrue(result.output().endsWith("[output cut off after 4096 bytes]"), result::output);
        assertTrue(result.output().length() < 4096 + 100);
    }

    @Test
    void allocatingPastTheHeapLimitCrashesWithOutOfMemory() throws Exception {
        TestRunResult result = run("memory-hog");

        assertEquals(Outcome.CRASHED, result.outcome());
        assertTrue(result.output().contains("java.lang.OutOfMemoryError"), result::output);
    }

    @Test
    void failureOutputLeavesOutTheLauncherSummary() throws Exception {
        TestRunResult result = run("failing");

        assertFalse(result.output().contains("Failures ("), result::output);
        assertFalse(result.output().contains("Test run finished"), result::output);
    }

    @Test
    void outputPrintedByTheLearnerIsKept() throws Exception {
        assertTrue(run("chatty").output().startsWith("line 0\nline 1\n"));
    }

    @Test
    void snippetPrintsItsOutput() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hi " + (2 + 3));
                    }
                }
                """);

        assertEquals(ProgramResult.Status.COMPLETED, result.status());
        assertEquals("Hi 5", result.output().strip());
    }

    @Test
    void snippetCanUseTheLibrariesOnTheRunnerClasspath() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Main {
                    public static void main(String[] args) {
                        org.junit.jupiter.api.Assertions.assertEquals(5, 2 + 3);
                        System.out.println("library ok");
                    }
                }
                """);

        assertEquals(ProgramResult.Status.COMPLETED, result.status(), result::output);
        assertEquals("library ok", result.output().strip());
    }

    @Test
    void mockitoOnTheClasspathIsLoadedAsAnAgent() {
        Path mockito = Path.of("/m2/org/mockito/mockito-core/5.24.0/mockito-core-5.24.0.jar");

        assertEquals(List.of("-javaagent:" + mockito, "-Xshare:off"),
                PracticeRunner.agentOptions(List.of(CONSOLE_JAR, mockito)));
        assertEquals(List.of(), PracticeRunner.agentOptions(List.of(CONSOLE_JAR)));
    }

    @Test
    void parameterNamesAreKeptForSpring() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Main {
                    static void find(String bikeId) { }
                    public static void main(String[] args) throws Exception {
                        System.out.println(Main.class.getDeclaredMethod("find", String.class).getParameters()[0].getName());
                    }
                }
                """);

        assertEquals("bikeId", result.output().strip(), "javac should run with -parameters");
    }

    @Test
    void snippetFileIsNamedAfterItsClass() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Kiosk {
                    public static void main(String[] args) {
                        System.out.println("ok");
                    }
                }
                """);

        assertEquals(ProgramResult.Status.COMPLETED, result.status(), result::toString);
    }

    @Test
    void uncaughtExceptionIsReportedWithItsMessage() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Main {
                    public static void main(String[] args) {
                        int[] docks = new int[3];
                        docks[5] = 1;
                    }
                }
                """);

        assertEquals(ProgramResult.Status.EXCEPTION, result.status());
        assertTrue(result.output().contains("ArrayIndexOutOfBoundsException"), result::output);
    }

    @Test
    void snippetCompileErrorPointsAtTheLine() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hi")
                    }
                }
                """);

        assertEquals(ProgramResult.Status.COMPILE_ERROR, result.status());
        assertEquals(3, result.compileErrors().getFirst().line());
    }

    @Test
    void endlessProgramIsStopped() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Main {
                    public static void main(String[] args) {
                        while (true) { }
                    }
                }
                """);

        assertEquals(ProgramResult.Status.TIMED_OUT, result.status());
    }

    @Test
    void decimalsAlwaysUseAPointWhateverTheMacLocale() throws Exception {
        ProgramResult result = runner.runSnippet("""
                public class Main {
                    public static void main(String[] args) {
                        System.out.println(String.format("%.2f", 2.5));
                    }
                }
                """);

        assertEquals("2.50", result.output().strip());
    }
}
