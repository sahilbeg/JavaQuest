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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Runs JavaScript and React practice code with Node.
 *
 * <p>The workspace is tracks/web/practice: its package.json pins Vitest, jsdom, React Testing Library and esbuild, and
 * run.sh installs them with npm ci. Each run copies the ticket's src/ and test/ into a fresh folder inside the
 * workspace (so imports find node_modules), checks the syntax with esbuild like javac would, then runs Vitest in jsdom
 * with its JUnit report, which the same parser as the Java runner reads.
 */
public final class NodeRunner implements CodeRunner {

    /** tsc's plain output: src/cart.ts(4,14): error TS2322: Type 'string' is not assignable to type 'number'. */
    private static final Pattern TS_ERROR = Pattern.compile("(?m)^(.+?)\\((\\d+),\\d+\\): error (TS\\d+): (.+)$");

    private static final Pattern ERROR_LINE = Pattern.compile("\\{\"file\":\"((?:[^\"\\\\]|\\\\.)*)\",\"line\":(\\d+),\"message\":\"((?:[^\"\\\\]|\\\\.)*)\"\\}");

    private final Path node;
    private final Path workspace;
    private final RunLimits limits;

    public NodeRunner(Path node, Path workspace, RunLimits limits) {
        this.node = node;
        this.workspace = workspace.toAbsolutePath().normalize();
        this.limits = limits;
    }

    /** The node on the PATH, which run.sh checks is version 20 or newer. */
    public static Path nodeOnPath() {
        return Path.of("node");
    }

    @Override
    public RunLimits limits() {
        return limits;
    }

    /** tracks/web/practice: package.json, node_modules and the tools. */
    public Path workspace() {
        return workspace;
    }

    /** Whether npm ci has installed the tools, so the web courses can run. */
    public boolean ready() {
        return Files.isDirectory(workspace.resolve("node_modules/vitest")) && Files.isDirectory(workspace.resolve("node_modules/esbuild"));
    }

    @Override
    public TestRunResult run(Path mainSources, Path testSources, RunLimits limits) throws IOException, InterruptedException {
        long started = System.nanoTime();
        Path work = newWorkDir();
        try {
            copyTree(mainSources, work.resolve("src"));
            copyTree(testSources, work.resolve("test"));
            List<CompileError> errors = syntaxErrors(work.resolve("src"), limits);
            if (errors.isEmpty()) {
                errors = typeErrors(work, limits);
            }
            if (!errors.isEmpty()) {
                return new TestRunResult(Outcome.COMPILE_ERROR, List.of(), errors, "", elapsedSince(started));
            }
            Files.writeString(work.resolve("vitest.config.mjs"), """
                    export default {
                      test: {
                        root: import.meta.dirname,
                        environment: "jsdom",
                        include: ["test/**/*.test.{js,jsx,ts,tsx}"],
                        reporters: ["junit"],
                        outputFile: "report.xml",
                        testTimeout: %d,
                        watch: false,
                      },
                    };
                    """.formatted(limits.timeout().toMillis()));
            Finished run = spawn(List.of(node.toString(), workspace.resolve("node_modules/vitest/vitest.mjs").toString(), "run",
                    "--config", work.resolve("vitest.config.mjs").toString()), work, limits, "");
            Duration duration = elapsedSince(started);
            if (!run.inTime()) {
                return new TestRunResult(Outcome.TIMED_OUT, List.of(), List.of(), run.output(), duration);
            }
            Path report = work.resolve("report.xml");
            if (!Files.exists(report)) {
                return new TestRunResult(Outcome.CRASHED, List.of(), List.of(), run.output(), duration);
            }
            List<TestCaseResult> tests;
            try (InputStream in = Files.newInputStream(report)) {
                tests = JUnitReportParser.parse(in).stream().map(NodeRunner::shortName).toList();
            }
            if (tests.isEmpty()) {
                return new TestRunResult(Outcome.CRASHED, tests, List.of(), run.output(), duration);
            }
            Outcome outcome = tests.stream().allMatch(TestCaseResult::passed) ? Outcome.PASSED : Outcome.FAILED;
            return new TestRunResult(outcome, tests, List.of(), run.output(), duration);
        } finally {
            deleteRecursively(work);
        }
    }

    @Override
    public ProgramResult runMain(Path sources, String entry, RunLimits limits, String input) throws IOException, InterruptedException {
        long started = System.nanoTime();
        Path work = newWorkDir();
        try {
            copyTree(sources, work.resolve("src"));
            List<CompileError> errors = syntaxErrors(work.resolve("src"), limits);
            if (!errors.isEmpty()) {
                return new ProgramResult(ProgramResult.Status.COMPILE_ERROR, errors, "", elapsedSince(started));
            }
            Finished run = spawn(List.of(node.toString(), work.resolve("src").resolve(entry).toString()), work, limits, input);
            ProgramResult.Status status = !run.inTime() ? ProgramResult.Status.TIMED_OUT
                    : run.exitCode() == 0 ? ProgramResult.Status.COMPLETED : ProgramResult.Status.EXCEPTION;
            return new ProgramResult(status, List.of(), run.output(), elapsedSince(started));
        } finally {
            deleteRecursively(work);
        }
    }

    /**
     * A TypeScript ticket (any .ts or .tsx in src/) is type-checked with tsc, strict, before its checks run, so a type
     * error shows like a Java compile error, with its file and line. JavaScript tickets skip this.
     */
    private List<CompileError> typeErrors(Path work, RunLimits limits) throws IOException, InterruptedException {
        boolean typeScript;
        try (Stream<Path> files = Files.walk(work.resolve("src"))) {
            typeScript = files.anyMatch(f -> f.toString().endsWith(".ts") || f.toString().endsWith(".tsx"));
        }
        if (!typeScript) {
            return List.of();
        }
        Files.writeString(work.resolve("tsconfig.json"), """
                {
                  "compilerOptions": {
                    "strict": true, "noEmit": true, "target": "es2022", "module": "esnext", "moduleResolution": "bundler",
                    "jsx": "react-jsx", "skipLibCheck": true, "allowJs": false, "allowImportingTsExtensions": true, "types": [], "lib": ["es2023", "dom", "dom.iterable"]
                  },
                  "include": ["src/**/*"]
                }
                """);
        Finished check = spawn(List.of(node.toString(), workspace.resolve("node_modules/typescript/bin/tsc").toString(),
                "-p", work.resolve("tsconfig.json").toString(), "--pretty", "false"), work, limits, "");
        var errors = new ArrayList<CompileError>();
        Matcher m = TS_ERROR.matcher(check.output());
        while (m.find()) {
            String file = m.group(1).replace('\\', '/');
            file = file.startsWith("src/") ? file.substring(4) : file;
            errors.add(new CompileError(file, Long.parseLong(m.group(2)), m.group(3) + ": " + m.group(4)));
        }
        return errors;
    }

    /** Runs a TypeScript lesson snippet: Node strips the types and runs it. */
    public ProgramResult runTypeScriptSnippet(String code, String input) throws IOException, InterruptedException {
        Path dir = Files.createTempDirectory("cadence-ts-snippet-");
        try {
            Files.writeString(dir.resolve("main.ts"), code);
            return runMain(dir, "main.ts", limits, input);
        } finally {
            deleteRecursively(dir);
        }
    }

    @Override
    public ProgramResult runSnippet(String code, String input) throws IOException, InterruptedException {
        Path dir = Files.createTempDirectory("cadence-js-snippet-");
        try {
            Files.writeString(dir.resolve("main.js"), code);
            return runMain(dir, "main.js", limits, input);
        } finally {
            deleteRecursively(dir);
        }
    }

    /** The syntax errors in a piece of JavaScript (or JSX), without running it: for checking lesson code. */
    public List<CompileError> check(String code) throws IOException, InterruptedException {
        return check(code, "main.js");
    }

    /** Syntax errors in lesson code; {@code fileName} main.ts checks TypeScript syntax. */
    public List<CompileError> check(String code, String fileName) throws IOException, InterruptedException {
        Path work = newWorkDir();
        try {
            Files.createDirectories(work.resolve("src"));
            Files.writeString(work.resolve("src").resolve(fileName), code);
            return syntaxErrors(work.resolve("src"), limits);
        } finally {
            deleteRecursively(work);
        }
    }

    /** Vitest names a test "describe > test"; the check list shows the test, like JUnit's display name. */
    private static TestCaseResult shortName(TestCaseResult test) {
        int at = test.name().lastIndexOf(" > ");
        return at < 0 ? test : new TestCaseResult(test.className(), test.name().substring(at + 3), test.passed(), test.message());
    }

    private List<CompileError> syntaxErrors(Path src, RunLimits limits) throws IOException, InterruptedException {
        Finished check = spawn(List.of(node.toString(), workspace.resolve("tools/check.mjs").toString(), src.toString()),
                src.getParent(), limits, "");
        var errors = new ArrayList<CompileError>();
        Matcher m = ERROR_LINE.matcher(check.output());
        while (m.find()) {
            errors.add(new CompileError(unescape(m.group(1)), Long.parseLong(m.group(2)), unescape(m.group(3))));
        }
        return errors;
    }

    private static String unescape(String json) {
        return json.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }

    private Path newWorkDir() throws IOException {
        Path runs = Files.createDirectories(workspace.resolve(".runs"));
        return Files.createDirectory(runs.resolve(UUID.randomUUID().toString()));
    }

    private record Finished(boolean inTime, int exitCode, String output) {}

    private Finished spawn(List<String> command, Path dir, RunLimits limits, String input) throws IOException, InterruptedException {
        var builder = new ProcessBuilder(command).directory(dir.toFile()).redirectErrorStream(true);
        builder.environment().put("NODE_OPTIONS", "--max-old-space-size=" + Math.max(limits.heapMegabytes(), 256));
        builder.environment().put("NO_COLOR", "1");
        builder.environment().put("FORCE_COLOR", "0");
        builder.environment().put("CI", "1");
        Process process = builder.start();
        var output = new CappedOutput(process.getInputStream(), limits.maxOutputBytes());
        Thread reader = Thread.ofVirtual().start(output::drain);
        byte[] stdin = input == null ? new byte[0] : input.getBytes(StandardCharsets.UTF_8);
        Thread.ofVirtual().start(() -> {
            try (var in = process.getOutputStream()) {
                in.write(stdin);
            } catch (IOException ignored) {
                // The program ended before reading everything.
            }
        });
        // Vitest starts workers, so allow it a few seconds on top of the per-test timeout.
        long waitMillis = limits.timeout().toMillis() + 15_000;
        boolean finished = process.waitFor(waitMillis, TimeUnit.MILLISECONDS);
        if (!finished) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            process.waitFor();
        }
        reader.join();
        return new Finished(finished, process.exitValue(), output.text());
    }

    private static Duration elapsedSince(long startedNanos) {
        return Duration.ofNanos(System.nanoTime() - startedNanos);
    }

    private static void copyTree(Path from, Path to) throws IOException {
        Files.createDirectories(to);
        if (!Files.isDirectory(from)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(from)) {
            for (Path path : paths.toList()) {
                Path target = to.resolve(from.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(path, target);
                }
            }
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
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

    /** Keeps reading after the cap so a chatty program never blocks on a full pipe. */
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
