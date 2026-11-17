package dev.javaquest.career;

import dev.javaquest.career.GameRules.Card;
import dev.javaquest.career.GameRules.Streak;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CareerStore {

    public record DueCard(String topicId, Card card, String lastResult) {}

    private final JdbcClient jdbc;

    public CareerStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Records XP once per dedupe key. Returns false when the key was already used. */
    public boolean addXp(String kind, int amount, String topicId, String dedupeKey, LocalDate day, Instant now) {
        int existing = jdbc.sql("SELECT COUNT(*) FROM xp_event WHERE dedupe_key = ?").param(dedupeKey).query(Integer.class).single();
        if (existing > 0) {
            return false;
        }
        jdbc.sql("INSERT INTO xp_event (kind, amount, topic_id, dedupe_key, earned_on, created_at) VALUES (?, ?, ?, ?, ?, ?)")
                .params(kind, amount, topicId, dedupeKey, Date.valueOf(day), now.atOffset(ZoneOffset.UTC))
                .update();
        return true;
    }

    public int totalXp() {
        return jdbc.sql("SELECT COALESCE(SUM(amount), 0) FROM xp_event").query(Integer.class).single();
    }

    public int xpOn(LocalDate day) {
        return jdbc.sql("SELECT COALESCE(SUM(amount), 0) FROM xp_event WHERE earned_on = ?").param(Date.valueOf(day)).query(Integer.class).single();
    }

    public int countXp(String kind) {
        return jdbc.sql("SELECT COUNT(*) FROM xp_event WHERE kind = ?").param(kind).query(Integer.class).single();
    }

    public Streak streak() {
        return jdbc.sql("SELECT streak, longest_streak, last_active, freezes FROM player WHERE id = 1")
                .query((rs, n) -> new Streak(rs.getInt(1), rs.getInt(2),
                        Optional.ofNullable(rs.getDate(3)).map(Date::toLocalDate).orElse(null), rs.getInt(4)))
                .single();
    }

    public void saveStreak(Streak s) {
        jdbc.sql("UPDATE player SET streak = ?, longest_streak = ?, last_active = ?, freezes = ? WHERE id = 1")
                .params(s.current(), s.longest(), s.lastActive() == null ? null : Date.valueOf(s.lastActive()), s.freezes())
                .update();
    }

    public int spotBugsCorrect() {
        return jdbc.sql("SELECT spot_bugs_correct FROM player WHERE id = 1").query(Integer.class).single();
    }

    public void addSpotBugsCorrect(int count) {
        jdbc.sql("UPDATE player SET spot_bugs_correct = spot_bugs_correct + ? WHERE id = 1").param(count).update();
    }

    public Set<String> kudos() {
        return new TreeSet<>(jdbc.sql("SELECT id FROM kudos").query(String.class).list());
    }

    public void addKudos(String id, Instant now) {
        jdbc.sql("MERGE INTO kudos (id, earned_at) KEY (id) VALUES (?, ?)").params(id, now.atOffset(ZoneOffset.UTC)).update();
    }

    public Set<String> masteredTopics() {
        return new TreeSet<>(jdbc.sql("SELECT topic_id FROM topic_progress WHERE mastered_at IS NOT NULL").query(String.class).list());
    }

    public int idePasses() {
        return jdbc.sql("SELECT COUNT(DISTINCT topic_id) FROM test_run WHERE source = 'IDE' AND outcome = 'PASSED'").query(Integer.class).single();
    }

    public Optional<Card> card(String topicId) {
        return jdbc.sql("SELECT ease, interval_days, repetitions, due_on FROM review_card WHERE topic_id = ?")
                .param(topicId)
                .query((rs, n) -> new Card(rs.getDouble(1), rs.getInt(2), rs.getInt(3), rs.getDate(4).toLocalDate()))
                .optional();
    }

    public void saveCard(String topicId, Card card, String lastResult) {
        jdbc.sql("""
                        MERGE INTO review_card (topic_id, ease, interval_days, repetitions, due_on, last_result)
                        KEY (topic_id) VALUES (?, ?, ?, ?, ?, ?)""")
                .params(topicId, card.ease(), card.intervalDays(), card.repetitions(), Date.valueOf(card.dueOn()), lastResult)
                .update();
    }

    public List<DueCard> dueCards(LocalDate today) {
        return jdbc.sql("""
                        SELECT topic_id, ease, interval_days, repetitions, due_on, last_result
                        FROM review_card WHERE due_on <= ? ORDER BY due_on, topic_id""")
                .param(Date.valueOf(today))
                .query((rs, n) -> new DueCard(rs.getString(1),
                        new Card(rs.getDouble(2), rs.getInt(3), rs.getInt(4), rs.getDate(5).toLocalDate()), rs.getString(6)))
                .list();
    }

    public List<String> lastWeakConcepts(String topicId) {
        return jdbc.sql("SELECT weak_concepts FROM quiz_attempt WHERE topic_id = ? ORDER BY id DESC LIMIT 1")
                .param(topicId)
                .query(String.class)
                .optional()
                .filter(text -> !text.isBlank())
                .map(text -> List.of(text.split(",")))
                .orElse(List.of());
    }

    public Set<String> topicsWithLastReview(String result) {
        return new TreeSet<>(jdbc.sql("SELECT topic_id FROM review_card WHERE last_result = ?").param(result).query(String.class).list());
    }

    public static final List<String> EXPORTED_TABLES =
            List.of("topic_progress", "test_run", "quiz_attempt", "xp_event", "kudos", "player", "review_card",
                    "lesson_position");

    public List<Map<String, Object>> dump(String table) {
        requireKnown(table);
        return jdbc.sql("SELECT * FROM " + table).query().listOfRows().stream().map(CareerStore::portable).toList();
    }

    public void replace(String table, List<Map<String, Object>> rows) {
        requireKnown(table);
        jdbc.sql("DELETE FROM " + table).update();
        for (Map<String, Object> row : rows) {
            var columns = List.copyOf(row.keySet());
            if (!columns.stream().allMatch(c -> c.matches("[A-Za-z_]+"))) {
                throw new IllegalArgumentException("Bad column name in " + table);
            }
            String sql = "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES ("
                    + String.join(", ", columns.stream().map(c -> "?").toList()) + ")";
            jdbc.sql(sql).params(columns.stream().map(row::get).toList()).update();
        }
    }

    /** Dates stay calendar dates and times stay instants, so an export never shifts a day across time zones. */
    private static Map<String, Object> portable(Map<String, Object> row) {
        var out = new java.util.LinkedHashMap<String, Object>();
        row.forEach((column, value) -> out.put(column, switch (value) {
            case Date date -> date.toLocalDate().toString();
            case java.time.OffsetDateTime time -> time.toInstant().toString();
            case java.sql.Timestamp time -> time.toInstant().toString();
            case java.sql.Clob clob -> clobText(clob);
            case null -> null;
            default -> value;
        }));
        return out;
    }

    private static String clobText(java.sql.Clob clob) {
        try {
            return clob.getSubString(1, (int) clob.length());
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void requireKnown(String table) {
        if (!EXPORTED_TABLES.contains(table)) {
            throw new IllegalArgumentException("Unknown table: " + table);
        }
    }
}
