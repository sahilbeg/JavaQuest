package dev.javaquest.progress;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ProgressStore {

    public record Row(String topicId, int quizBest, int quizAttempts, boolean testsPassed, int failedRuns,
                      int hintsUsed, int failedRunsAtLastHint, boolean solutionViewed, Instant masteredAt) {

        static Row empty(String topicId) {
            return new Row(topicId, 0, 0, false, 0, 0, 0, false, null);
        }
    }

    private final JdbcClient jdbc;

    public ProgressStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Row row(String topicId) {
        return jdbc.sql("""
                        SELECT topic_id, quiz_best, quiz_attempts, tests_passed, failed_runs, hints_used,
                               failed_runs_at_last_hint, solution_viewed, mastered_at
                        FROM topic_progress WHERE topic_id = ?""")
                .param(topicId)
                .query((rs, n) -> new Row(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getBoolean(4), rs.getInt(5),
                        rs.getInt(6), rs.getInt(7), rs.getBoolean(8),
                        Optional.ofNullable(rs.getObject(9, OffsetDateTime.class)).map(OffsetDateTime::toInstant).orElse(null)))
                .optional()
                .orElse(Row.empty(topicId));
    }

    public void save(Row row, Instant now) {
        jdbc.sql("""
                        MERGE INTO topic_progress (topic_id, quiz_best, quiz_attempts, tests_passed, failed_runs, hints_used,
                                                   failed_runs_at_last_hint, solution_viewed, mastered_at, last_seen)
                        KEY (topic_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""")
                .params(row.topicId(), row.quizBest(), row.quizAttempts(), row.testsPassed(), row.failedRuns(),
                        row.hintsUsed(), row.failedRunsAtLastHint(), row.solutionViewed(),
                        row.masteredAt() == null ? null : row.masteredAt().atOffset(ZoneOffset.UTC), now.atOffset(ZoneOffset.UTC))
                .update();
    }

    public void addTestRun(String topicId, String source, String outcome, int passed, int total, long durationMs,
                           String resultJson, Instant now) {
        jdbc.sql("""
                        INSERT INTO test_run (topic_id, source, outcome, passed, total, duration_ms, result_json, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)""")
                .params(topicId, source, outcome, passed, total, durationMs, resultJson, now.atOffset(ZoneOffset.UTC))
                .update();
    }

    public Optional<String> latestRunJson(String topicId) {
        return jdbc.sql("SELECT result_json FROM test_run WHERE topic_id = ? ORDER BY id DESC LIMIT 1")
                .param(topicId)
                .query(String.class)
                .optional();
    }

    public void addQuizAttempt(String topicId, long seed, int score, boolean passed, List<String> weakConcepts, Instant now) {
        jdbc.sql("""
                        INSERT INTO quiz_attempt (topic_id, seed, score, passed, weak_concepts, created_at)
                        VALUES (?, ?, ?, ?, ?, ?)""")
                .params(topicId, seed, score, passed, String.join(",", weakConcepts), now.atOffset(ZoneOffset.UTC))
                .update();
    }
}
