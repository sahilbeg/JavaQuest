package dev.cadence.career;

import dev.cadence.content.ContentCatalog;
import dev.cadence.content.Topic;
import dev.cadence.progress.ProgressService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Everything the career dashboard shows, read in one place. */
@Component
public class CareerReport {

    /** {@code ladder} is every job title from Intern up, so the page can draw the whole career ladder. */
    public record LevelView(int xp, int level, String title, int levelStartXp, int nextLevelXp, String nextTitle, int nextTitleLevel,
                            List<GameRules.Rung> ladder) {}

    public record StreakView(int current, int longest, int freezes, boolean activeToday, int xpToday, int dailyGoal) {}

    /** {@code earnedOn} is the local date the kudos was earned, or null while it is not. */
    public record KudosView(String id, String title, String from, String message, String howToEarn, boolean earned,
                            LocalDate earnedOn) {}

    public record TopicView(String id, String key, String title, int masteryPercent, boolean mastered) {}

    public record WeakView(String id, String key, String title, String reason, List<String> concepts) {}

    public record ReviewView(String id, String key, String title) {}

    public record Summary(LevelView level, StreakView streak, List<KudosView> kudos, List<TopicView> topics,
                          List<WeakView> weak, List<ReviewView> reviewsDue) {}

    private final CareerStore store;
    private final CareerService career;
    private final ProgressService progress;
    private final ContentCatalog catalog;
    private final Clock clock;

    public CareerReport(CareerStore store, CareerService career, ProgressService progress, ContentCatalog catalog, Clock clock) {
        this.store = store;
        this.career = career;
        this.progress = progress;
        this.catalog = catalog;
        this.clock = clock;
    }

    public Summary summary() {
        LocalDate today = LocalDate.now(clock);
        int xp = store.totalXp();
        int level = GameRules.level(xp);
        int nextTitleLevel = ((level - 1) / GameRules.LEVELS_PER_TITLE + 1) * GameRules.LEVELS_PER_TITLE + 1;
        var levelView = new LevelView(xp, level, GameRules.jobTitle(level), GameRules.xpForLevel(level),
                GameRules.xpForLevel(level + 1), GameRules.jobTitle(nextTitleLevel), nextTitleLevel, GameRules.ladder());

        var streak = store.streak();
        var streakView = new StreakView(GameRules.currentStreak(streak, today), streak.longest(), streak.freezes(),
                today.equals(streak.lastActive()), store.xpOn(today), GameRules.DAILY_XP_FOR_STREAK);

        var earnedAt = store.kudosEarnedAt();
        var earned = earnedAt.keySet();
        java.util.function.Function<String, LocalDate> earnedOn =
                id -> earnedAt.containsKey(id) ? LocalDate.ofInstant(earnedAt.get(id), clock.getZone()) : null;
        var kudos = new ArrayList<KudosView>();
        for (var k : Kudos.ALL) {
            kudos.add(new KudosView(k.id(), k.title(), k.from(), k.message(), k.howToEarn(), earned.contains(k.id()),
                    earnedOn.apply(k.id())));
        }
        for (var module : catalog.modules()) {
            String id = Kudos.projectShippedId(module.id());
            kudos.add(new KudosView(id, "Shipped: " + module.project(), "priya",
                    "Every ticket in " + module.project() + " is done. That project shipped!",
                    "Close every ticket in " + module.project(), earned.contains(id), earnedOn.apply(id)));
        }

        var topics = new ArrayList<TopicView>();
        var weak = new ArrayList<WeakView>();
        var again = store.topicsWithLastReview(GameRules.ReviewGrade.AGAIN.name());
        for (Topic topic : catalog.topics()) {
            var p = progress.progress(topic);
            var ticket = topic.meta().ticket();
            topics.add(new TopicView(topic.id(), ticket.key(), ticket.title(), p.masteryPercent(), p.mastered()));
            String reason = again.contains(topic.id()) ? "Your last review missed some questions."
                    : p.quizAttempts() >= 2 && p.quizBest() < topic.passPercent() ? "The onboarding check isn't passed yet."
                    : null;
            if (reason != null) {
                weak.add(new WeakView(topic.id(), ticket.key(), ticket.title(), reason, store.lastWeakConcepts(topic.id())));
            }
        }

        var due = store.dueCards(today).stream()
                .map(card -> catalog.topic(card.topicId()).orElse(null))
                .filter(t -> t != null)
                .map(t -> new ReviewView(t.id(), t.meta().ticket().key(), t.meta().ticket().title()))
                .toList();
        return new Summary(levelView, streakView, kudos, topics, weak, due);
    }
}
