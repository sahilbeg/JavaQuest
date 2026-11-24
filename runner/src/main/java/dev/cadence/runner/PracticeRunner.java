package dev.cadence.runner;

import dev.cadence.runner.TestRunResult.CompileError;
import dev.cadence.runner.TestRunResult.Outcome;
import dev.cadence.runner.TestRunResult.TestCaseResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * Compiles a learner's sources and their tests, then runs the tests in a separate JVM with time, memory
 * and output limits. This protects against accidents such as infinite loops. It does not stop code that
 * reads files or opens network connections on purpose.
 */
public final class PracticeRunner implements CodeRunner {

    private static final String REPORT_FILE = "TEST-junit-jupiter.xml";

    private final Path javaExecutable;
    private final List<Path> testRuntimeClasspath;
    private final RunLimits limits;

    public PracticeRunner(Path javaExecutable, List<Path> testRuntimeClasspath, RunLimits limits) {
        this.javaExecutable = javaExecutable;
        this.testRuntimeClasspath = List.copyOf(testRuntimeClasspath);
        this.limits = limits;
    }

    /** The limits every run gets unless a caller passes its own. */
    public RunLimits limits() {
        return limits;
    }

    public static Path currentJavaExecutable() {
        return Path.of(System.getProperty("java.home"), "bin", "java");
    }

    public TestRunResult run(Path mainSources, Path testSources) throws IOException, InterruptedException {
        return run(mainSources, testSources, limits.timeout());
    }

    public TestRunResult run(Path mainSources, Path testSources, Duration timeout) throws IOException, InterruptedException {
        return run(mainSources, testSources, limits.withTimeout(timeout));
    }

    /** Runs the tests with these limits instead of the runner's own, for topics that need more time or memory. */
    public TestRunResult run(Path mainSources, Path testSources, RunLimits limits) throws IOException, InterruptedException {
        mainSources = mainSources.toAbsolutePath().normalize();
        testSources = testSources.toAbsolutePath().normalize();
        long started = System.nanoTime();
        Path workDir = Files.createTempDirectory("cadence-run-");
        try {
            Path mainClasses = workDir.resolve("main");
            Path testClasses = workDir.resolve("test");
            List<CompileError> errors = compile(mainSources, mainClasses, testRuntimeClasspath);
            if (errors.isEmpty()) {
                var testCompileClasspath = new ArrayList<>(testRuntimeClasspath);
                testCompileClasspath.add(mainClasses);
                errors = compile(testSources, testClasses, testCompileClasspath);
            }
            if (!errors.isEmpty()) {
                return new TestRunResult(Outcome.COMPILE_ERROR, List.of(), errors, "", elapsedSince(started));
            }
            return runTests(workDir, mainClasses, testClasses, started, limits);
        } finally {
            deleteRecursively(workDir);
        }
    }

    private static List<CompileError> compile(Path sourceRoot, Path outputDir, List<Path> classpath) throws IOException {
        Files.createDirectories(outputDir);
        List<Path> sources;
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            sources = files.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
        if (sources.isEmpty()) {
            return List.of(new CompileError(sourceRoot.toString(), 0, "No .java files found"));
        }
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var fileManager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH, StandardCharsets.UTF_8)) {
            // -parameters keeps parameter names in the class files, as Spring Boot's Maven setup does,
            // so @PathVariable String id works without repeating the name.
            List<String> options = List.of(
                    "--release", "21", "-proc:none", "-parameters", "-encoding", "UTF-8",
                    "-d", outputDir.toString(),
                    "-cp", joinClasspath(classpath));
            var units = fileManager.getJavaFileObjectsFromPaths(sources);
            boolean ok = compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
            var errors = diagnostics.getDiagnostics().stream()
                    .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                    .map(d -> new CompileError(fileName(sourceRoot, d), d.getLineNumber(), d.getMessage(Locale.ENGLISH)))
                    .toList();
            if (!ok && errors.isEmpty()) {
                return List.of(new CompileError(sourceRoot.toString(), 0, "Compilation failed"));
            }
            return errors;
        }
    }

    private static String fileName(Path sourceRoot, Diagnostic<? extends JavaFileObject> diagnostic) {
        if (diagnostic.getSource() == null) {
            return "";
        }
        Path file = Path.of(diagnostic.getSource().toUri());
        return file.startsWith(sourceRoot) ? sourceRoot.relativize(file).toString() : file.getFileName().toString();
    }

    public ProgramResult runMain(Path sources, String mainClass) throws IOException, InterruptedException {
        return runMain(sources, mainClass, limits);
    }

    /** Runs a main method with these limits instead of the runner's own. */
    public ProgramResult runMain(Path sources, String mainClass, RunLimits limits) throws IOException, InterruptedException {
        return runMain(sources, mainClass, limits, "");
    }

    /**
     * Runs a main method and feeds {@code input} to its standard input, as if the learner typed it.
     * With no input the program sees the end of input at once, so a Scanner read throws NoSuchElementException.
     */
    public ProgramResult runMain(Path sources, String mainClass, RunLimits limits, String input)
            throws IOException, InterruptedException {
        sources = sources.toAbsolutePath().normalize();
        long started = System.nanoTime();
        Path workDir = Files.createTempDirectory("cadence-main-");
        try {
            Path classes = workDir.resolve("main");
            List<CompileError> errors = compile(sources, classes, testRuntimeClasspath);
            if (!errors.isEmpty()) {
                return new ProgramResult(ProgramResult.Status.COMPILE_ERROR, errors, "", elapsedSince(started));
            }
            var classpath = new ArrayList<>(testRuntimeClasspath);
            classpath.add(classes);
            Finished run = spawn(jvmCommand(workDir, classpath, List.of(mainClass), limits), workDir, limits, input);
            ProgramResult.Status status = !run.inTime() ? ProgramResult.Status.TIMED_OUT
                    : run.exitCode() == 0 ? ProgramResult.Status.COMPLETED : ProgramResult.Status.EXCEPTION;
            return new ProgramResult(status, List.of(), run.output(), elapsedSince(started));
        } finally {
            deleteRecursively(workDir);
        }
    }

    /** Runs a single-file program such as a lesson snippet. The file is named after its first top-level class. */
    public ProgramResult runSnippet(String code) throws IOException, InterruptedException {
        return runSnippet(code, "");
    }

    /** Runs a single-file program and feeds {@code input} to its standard input. */
    public ProgramResult runSnippet(String code, String input) throws IOException, InterruptedException {
        String className = Snippets.mainClassName(code);
        Path dir = Files.createTempDirectory("cadence-snippet-");
        try {
            Files.writeString(dir.resolve(className + ".java"), code);
            return runMain(dir, className, limits, input);
        } finally {
            deleteRecursively(dir);
        }
    }

    private TestRunResult runTests(Path workDir, Path mainClasses, Path testClasses, long started, RunLimits limits)
            throws IOException, InterruptedException {
        Path reportsDir = workDir.resolve("reports");
        var classpath = new ArrayList<>(testRuntimeClasspath);
        classpath.add(mainClasses);
        classpath.add(testClasses);
        Finished run = spawn(jvmCommand(workDir, classpath, List.of(
                "org.junit.platform.console.ConsoleLauncher", "execute",
                "--scan-classpath", testClasses.toString(),
                "--reports-dir", reportsDir.toString(),
                "--disable-banner", "--details=none", "--disable-ansi-colors"), limits), workDir, limits);
        String text = LauncherOutput.withoutSummary(run.output());
        Duration duration = elapsedSince(started);
        if (!run.inTime()) {
            return new TestRunResult(Outcome.TIMED_OUT, List.of(), List.of(), text, duration);
        }
        Path report = reportsDir.resolve(REPORT_FILE);
        if (!Files.exists(report)) {
            return new TestRunResult(Outcome.CRASHED, List.of(), List.of(), text, duration);
        }
        List<TestCaseResult> tests;
        try (InputStream in = Files.newInputStream(report)) {
            tests = JUnitReportParser.parse(in);
        }
        if (tests.isEmpty()) {
            return new TestRunResult(Outcome.CRASHED, tests, List.of(), text, duration);
        }
        Outcome outcome = tests.stream().allMatch(TestCaseResult::passed) ? Outcome.PASSED : Outcome.FAILED;
        return new TestRunResult(outcome, tests, List.of(), text, duration);
    }

    private List<String> jvmCommand(Path workDir, List<Path> classpath, List<String> mainAndArgs, RunLimits limits) {
        var command = new ArrayList<>(List.of(
                javaExecutable.toString(),
                "-Xmx" + limits.heapMegabytes() + "m",
                "-Xss1m",
                "-XX:+UseSerialGC",
                "-XX:TieredStopAtLevel=1",
                "-Djava.io.tmpdir=" + workDir,
                "-Duser.language=en", "-Duser.country=US"));
        command.addAll(agentOptions(classpath));
        command.addAll(List.of("-cp", joinClasspath(classpath)));
        command.addAll(mainAndArgs);
        return command;
    }

    /** Mockito warns on every run unless it is loaded as an agent, and an agent turns off class sharing. */
    static List<String> agentOptions(List<Path> classpath) {
        return classpath.stream()
                .filter(entry -> entry.getFileName().toString().startsWith("mockito-core-"))
                .findFirst()
                .map(mockito -> List.of("-javaagent:" + mockito, "-Xshare:off"))
                .orElse(List.of());
    }

    private record Finished(boolean inTime, int exitCode, String output) {}

    private Finished spawn(List<String> command, Path workDir, RunLimits limits) throws IOException, InterruptedException {
        return spawn(command, workDir, limits, "");
    }

    private Finished spawn(List<String> command, Path workDir, RunLimits limits, String input)
            throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .redirectErrorStream(true)
                .start();
        var output = new CappedOutput(process.getInputStream(), limits.maxOutputBytes());
        Thread reader = Thread.ofVirtual().start(output::drain);
        byte[] stdin = input == null ? new byte[0] : input.getBytes(StandardCharsets.UTF_8);
        // Written on its own thread so a program that never reads its input cannot block the runner.
        Thread.ofVirtual().start(() -> {
            try (var in = process.getOutputStream()) {
                in.write(stdin);
            } catch (IOException ignored) {
                // The program ended before reading everything; that is its choice, not an error.
            }
        });
        boolean finished = process.waitFor(limits.timeout().toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            process.waitFor();
        }
        reader.join();
        return new Finished(finished, process.exitValue(), output.text());
    }

    private static String joinClasspath(List<Path> entries) {
        return String.join(java.io.File.pathSeparator, entries.stream().map(Path::toString).toList());
    }

    private static Duration elapsedSince(long startedNanos) {
        return Duration.ofNanos(System.nanoTime() - startedNanos);
    }

    private static void deleteRecursively(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    /** Keeps reading after the cap so a chatty child never blocks on a full pipe. */
    private static final class CappedOutput {
        private final InputStream in;
        private final int cap;
        private final ByteArrayOutputStream kept = new ByteArrayOutputStream();
        private boolean truncated;

        CappedOutput(InputStream in, int cap) {
            this.in = in;
            this.cap = cap;
        }

        void drain() {
            byte[] buffer = new byte[8192];
            try (in) {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    int room = cap - kept.size();
                    if (room > 0) {
                        kept.write(buffer, 0, Math.min(room, read));
                    }
                    if (read > room) {
                        truncated = true;
                    }
                }
            } catch (IOException ignored) {
                // The stream closes when the process is killed.
            }
        }

        String text() {
            String text = kept.toString(StandardCharsets.UTF_8);
            return truncated ? text + "\n[output cut off after " + cap + " bytes]" : text;
        }
    }
}
