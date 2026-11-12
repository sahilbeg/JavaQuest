package dev.javaquest.runner;

import dev.javaquest.runner.TestRunResult.CompileError;
import dev.javaquest.runner.TestRunResult.Outcome;
import dev.javaquest.runner.TestRunResult.TestCaseResult;
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
public final class PracticeRunner {

    private static final String REPORT_FILE = "TEST-junit-jupiter.xml";

    private final Path javaExecutable;
    private final List<Path> testRuntimeClasspath;
    private final RunLimits limits;

    public PracticeRunner(Path javaExecutable, List<Path> testRuntimeClasspath, RunLimits limits) {
        this.javaExecutable = javaExecutable;
        this.testRuntimeClasspath = List.copyOf(testRuntimeClasspath);
        this.limits = limits;
    }

    public static Path currentJavaExecutable() {
        return Path.of(System.getProperty("java.home"), "bin", "java");
    }

    public TestRunResult run(Path mainSources, Path testSources) throws IOException, InterruptedException {
        mainSources = mainSources.toAbsolutePath().normalize();
        testSources = testSources.toAbsolutePath().normalize();
        long started = System.nanoTime();
        Path workDir = Files.createTempDirectory("javaquest-run-");
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
            return runTests(workDir, mainClasses, testClasses, started);
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
            List<String> options = List.of(
                    "--release", "21", "-proc:none", "-encoding", "UTF-8",
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

    private TestRunResult runTests(Path workDir, Path mainClasses, Path testClasses, long started)
            throws IOException, InterruptedException {
        Path reportsDir = workDir.resolve("reports");
        var classpath = new ArrayList<>(testRuntimeClasspath);
        classpath.add(mainClasses);
        classpath.add(testClasses);
        var command = List.of(
                javaExecutable.toString(),
                "-Xmx" + limits.heapMegabytes() + "m",
                "-Xss1m",
                "-XX:+UseSerialGC",
                "-XX:TieredStopAtLevel=1",
                "-Djava.io.tmpdir=" + workDir,
                "-cp", joinClasspath(classpath),
                "org.junit.platform.console.ConsoleLauncher", "execute",
                "--scan-classpath", testClasses.toString(),
                "--reports-dir", reportsDir.toString(),
                "--disable-banner", "--details=none", "--disable-ansi-colors");
        Process process = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .redirectErrorStream(true)
                .start();
        var output = new CappedOutput(process.getInputStream(), limits.maxOutputBytes());
        Thread reader = Thread.ofVirtual().start(output::drain);

        boolean finished = process.waitFor(limits.timeout().toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            process.waitFor();
        }
        reader.join();
        String text = output.text();
        Duration duration = elapsedSince(started);
        if (!finished) {
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
