package dev.cadence.tools;

import dev.cadence.content.ContentCatalog;
import dev.cadence.runner.PracticeRunner;
import dev.cadence.runner.RunLimits;
import dev.cadence.runner.TestRunResult;
import dev.cadence.runner.TestRunResult.Outcome;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Checks every practice topic: its solution must pass its tests, and its skeleton must compile and fail them.
 * Writes the outcome into build_state.json and exits with status 1 if any topic fails the check.
 */
public final class VerifyPractice {

    private static final RunLimits LIMITS = new RunLimits(Duration.ofSeconds(30), 256, 65_536);
    private static final ObjectMapper JSON = JsonMapper.builder()
            .defaultPrettyPrinter(new DefaultPrettyPrinter(Separators.createDefaultInstance()
                    .withObjectNameValueSpacing(Separators.Spacing.AFTER)
                    .withObjectEmptySeparator("")
                    .withArrayEmptySeparator(""))
                    .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE))
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: VerifyPractice <repo root>");
            System.err.println("Run it with: ./mvnw -q -pl tools dependency:properties exec:java");
            System.exit(2);
        }
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        var runner = new PracticeRunner(PracticeRunner.currentJavaExecutable(), RunnerClasspath.read(root), LIMITS);
        var catalog = ContentCatalog.load(root.resolve("content"), root.resolve("practice"));
        Path stateFile = root.resolve("build_state.json");
        ObjectNode state = (ObjectNode) JSON.readTree(stateFile.toFile());
        boolean allVerified = true;

        for (Path topicDir : practiceTopics(root.resolve("practice"))) {
            String topicId = root.resolve("practice").relativize(topicDir).toString().replace('\\', '/');
            RunLimits limits = limitsFor(catalog, topicId);
            TestRunResult solution = runner.run(topicDir.resolve("solution"), topicDir.resolve("test"), limits);
            TestRunResult skeleton = runner.run(topicDir.resolve("skeleton"), topicDir.resolve("test"), limits);
            boolean solutionPasses = solution.outcome() == Outcome.PASSED;
            boolean skeletonFails = skeleton.outcome() == Outcome.FAILED;
            System.out.printf("%s: solution %s (%d ms), skeleton %s (%d ms)%n", topicId,
                    solution.outcome(), solution.duration().toMillis(),
                    skeleton.outcome(), skeleton.duration().toMillis());
            if (!solutionPasses || !skeletonFails) {
                allVerified = false;
                System.out.println("  NOT VERIFIED: solution must be PASSED and skeleton must be FAILED");
            }
            record(state, topicId, solutionPasses, skeletonFails);
        }

        state.put("lastUpdated", OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        JSON.writeValue(stateFile.toFile(), state);
        if (!allVerified) {
            System.exit(1);
        }
    }

    /** The same limits the app uses: the topic's own timeout, and more time and heap for Spring topics. */
    private static RunLimits limitsFor(ContentCatalog catalog, String topicId) {
        var practice = catalog.topic(topicId).map(topic -> topic.meta().practice()).orElse(null);
        if (practice == null) {
            return LIMITS;
        }
        return LIMITS.withTimeout(practice.timeout(LIMITS.timeout()))
                .withHeapMegabytes(practice.heapMegabytes(LIMITS.heapMegabytes()));
    }

    private static List<Path> practiceTopics(Path practiceRoot) throws IOException {
        try (Stream<Path> dirs = Files.walk(practiceRoot, 2)) {
            return dirs.filter(dir -> practiceRoot.relativize(dir).getNameCount() == 2)
                    .filter(dir -> Files.isDirectory(dir.resolve("solution")) && Files.isDirectory(dir.resolve("test")))
                    .sorted()
                    .toList();
        }
    }

    private static void record(ObjectNode state, String topicId, boolean solutionPasses, boolean skeletonFails) {
        for (JsonNode module : state.get("modules")) {
            for (JsonNode node : module.get("topics")) {
                if (node.get("id").asString().equals(topicId)) {
                    ObjectNode topic = (ObjectNode) node;
                    ObjectNode verification = topic.putObject("verification");
                    verification.put("solutionPasses", solutionPasses);
                    verification.put("skeletonFails", skeletonFails);
                    verification.put("verifiedAt", OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
                    topic.put("status", TopicStatus.afterVerification(topic.get("status").asString(),
                            solutionPasses && skeletonFails));
                    return;
                }
            }
        }
        throw new IllegalStateException("Topic " + topicId + " is in practice/ but not in build_state.json");
    }
}
