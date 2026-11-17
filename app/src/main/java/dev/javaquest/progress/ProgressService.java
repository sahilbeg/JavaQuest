package dev.javaquest.progress;

import dev.javaquest.career.CareerService;
import dev.javaquest.content.QuizSession;
import dev.javaquest.content.Topic;
import dev.javaquest.content.TopicMeta.PracticeKind;
import dev.javaquest.practice.PracticeResult;
import dev.javaquest.runner.TestRunResult.Outcome;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ProgressService {

    public record TopicProgress(int quizBest, int quizAttempts, boolean testsPassed, int failedRuns, int hintsUsed,
                                int hintsAvailable, int hintCount, boolean solutionAvailable, boolean solutionViewed,
                                boolean mastered, int masteryPercent) {}

    public record SourceFile(String path, String content) {}

    private final ProgressStore store;
    private final JsonMapper json;
    private final Clock clock;
    private final CareerService career;

    public ProgressService(ProgressStore store, JsonMapper json, Clock clock, CareerService career) {
        this.store = store;
        this.json = json;
        this.clock = clock;
        this.career = career;
    }

    public StudySettings settings() {
        return store.settings();
    }

    @Transactional
    public StudySettings saveSettings(StudySettings settings) {
        settings.validate();
        store.saveSettings(settings);
        return store.settings();
    }

    public Optional<LessonPosition> position(Topic topic) {
        return store.position(topic.id());
    }

    @Transactional
    public void savePosition(Topic topic, LessonPosition position) {
        position.validate();
        store.savePosition(topic.id(), position, clock.instant());
    }

    /** Saved positions, the most recent first. */
    public List<ProgressStore.SavedPosition> positions() {
        return store.positions();
    }

    public VoiceSettings voice() {
        return store.voice();
    }

    @Transactional
    public VoiceSettings saveVoice(VoiceSettings voice) {
        voice.validate();
        store.saveVoice(voice);
        return store.voice();
    }

    /** Tickets first completed today, in the laptop's time zone. A new day starts at midnight. */
    public int masteredToday() {
        LocalDate today = LocalDate.now(clock);
        ZoneId zone = clock.getZone();
        return store.masteredBetween(today.atStartOfDay(zone).toInstant(), today.plusDays(1).atStartOfDay(zone).toInstant());
    }

    public TopicProgress progress(Topic topic) {
        return view(topic, store.row(topic.id()));
    }

    @Transactional
    public void recordTestRun(Topic topic, PracticeResult practice) {
        Instant now = clock.instant();
        var result = practice.result();
        var tiers = BossTiers.passed(result.tests());
        boolean passed = topic.meta().isBoss() ? tiers.contains(BossTiers.Tier.BRONZE) : result.outcome() == Outcome.PASSED;
        store.addTestRun(topic.id(), practice.source().name(), result.outcome().name(), (int) result.passedCount(),
                result.tests().size(), result.duration().toMillis(), json.writeValueAsString(practice), now);
        var row = store.row(topic.id());
        var updated = withMastery(topic, new ProgressStore.Row(row.topicId(), row.quizBest(), row.quizAttempts(),
                row.testsPassed() || passed, row.failedRuns() + (passed ? 0 : 1), row.hintsUsed(),
                row.failedRunsAtLastHint(), row.solutionViewed(), row.masteredAt()), now);
        store.save(updated, now);
        if (passed && !row.testsPassed()) {
            career.onChecksPassedFirstTime(topic, row.hintsUsed(), row.solutionViewed());
        }
        if (topic.meta().isBoss()) {
            career.onBossTiers(topic, tiers);
        }
        notifyIfNewlyMastered(topic, row, updated);
    }

    private void notifyIfNewlyMastered(Topic topic, ProgressStore.Row before, ProgressStore.Row after) {
        if (before.masteredAt() == null && after.masteredAt() != null) {
            career.onMastered(topic);
        }
    }

    public Optional<PracticeResult> latestRun(String topicId) {
        return store.latestRunJson(topicId).map(text -> json.readValue(text, PracticeResult.class));
    }

    @Transactional
    public TopicProgress recordQuiz(Topic topic, long seed, QuizSession.Result result) {
        Instant now = clock.instant();
        store.addQuizAttempt(topic.id(), seed, result.scorePercent(), result.passed(), result.weakConcepts(), now);
        var row = store.row(topic.id());
        var updated = withMastery(topic, new ProgressStore.Row(row.topicId(), Math.max(row.quizBest(), result.scorePercent()),
                row.quizAttempts() + 1, row.testsPassed(), row.failedRuns(), row.hintsUsed(), row.failedRunsAtLastHint(),
                row.solutionViewed(), row.masteredAt()), now);
        store.save(updated, now);
        career.onQuizGraded(topic, result);
        notifyIfNewlyMastered(topic, row, updated);
        return view(topic, updated);
    }

    /** Hints the learner has already opened. */
    public List<String> openedHints(Topic topic) {
        return topic.meta().hints().subList(0, Math.min(store.row(topic.id()).hintsUsed(), topic.meta().hints().size()));
    }

    @Transactional
    public String openNextHint(Topic topic) {
        var row = store.row(topic.id());
        int hintCount = topic.meta().hints().size();
        if (row.hintsUsed() >= hintCount) {
            throw new LockedException("You've opened every hint for this ticket.");
        }
        if (row.hintsUsed() >= ProgressRules.hintsAvailable(row.failedRuns(), hintCount)) {
            int needed = row.hintsUsed() + 1 == 2 ? ProgressRules.HINT_2_AFTER_FAILED_RUNS : ProgressRules.HINT_3_AFTER_FAILED_RUNS;
            throw new LockedException("Hint " + (row.hintsUsed() + 1) + " opens after " + needed
                    + " runs where the checks fail. You have " + row.failedRuns() + " so far. Have another go first!");
        }
        Instant now = clock.instant();
        store.save(new ProgressStore.Row(row.topicId(), row.quizBest(), row.quizAttempts(), row.testsPassed(),
                row.failedRuns(), row.hintsUsed() + 1, row.failedRuns(), row.solutionViewed(), row.masteredAt()), now);
        return topic.meta().hints().get(row.hintsUsed());
    }

    @Transactional
    public List<SourceFile> openSolution(Topic topic) throws IOException {
        var row = store.row(topic.id());
        if (!ProgressRules.solutionAvailable(row.testsPassed(), row.hintsUsed(), topic.meta().hints().size(),
                row.failedRuns(), row.failedRunsAtLastHint())) {
            throw new LockedException("The solution opens once your checks pass, or after all 3 hints and one more try.");
        }
        if (!row.testsPassed() && !row.solutionViewed()) {
            store.save(new ProgressStore.Row(row.topicId(), row.quizBest(), row.quizAttempts(), row.testsPassed(),
                    row.failedRuns(), row.hintsUsed(), row.failedRunsAtLastHint(), true, row.masteredAt()), clock.instant());
        }
        return javaFiles(topic.practiceSolution());
    }

    private static List<SourceFile> javaFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            var list = files.filter(p -> p.toString().endsWith(".java")).sorted().toList();
            var out = new java.util.ArrayList<SourceFile>();
            for (Path file : list) {
                out.add(new SourceFile(root.relativize(file).toString().replace('\\', '/'), Files.readString(file)));
            }
            return out;
        }
    }

    private ProgressStore.Row withMastery(Topic topic, ProgressStore.Row row, Instant now) {
        boolean mastered = ProgressRules.mastered(row.quizBest(), topic.passPercent(), row.testsPassed(), isCode(topic), topic.hasQuiz());
        if (!mastered || row.masteredAt() != null) {
            return row;
        }
        return new ProgressStore.Row(row.topicId(), row.quizBest(), row.quizAttempts(), row.testsPassed(), row.failedRuns(),
                row.hintsUsed(), row.failedRunsAtLastHint(), row.solutionViewed(), now);
    }

    private TopicProgress view(Topic topic, ProgressStore.Row row) {
        int hintCount = topic.meta().hints().size();
        int passPercent = topic.passPercent();
        return new TopicProgress(row.quizBest(), row.quizAttempts(), row.testsPassed(), row.failedRuns(), row.hintsUsed(),
                ProgressRules.hintsAvailable(row.failedRuns(), hintCount), hintCount,
                ProgressRules.solutionAvailable(row.testsPassed(), row.hintsUsed(), hintCount, row.failedRuns(), row.failedRunsAtLastHint()),
                row.solutionViewed(), row.masteredAt() != null,
                ProgressRules.masteryPercent(row.quizBest(), passPercent, row.testsPassed(), isCode(topic), topic.hasQuiz()));
    }

    private static boolean isCode(Topic topic) {
        return topic.meta().practice().kind() == PracticeKind.CODE;
    }
}
