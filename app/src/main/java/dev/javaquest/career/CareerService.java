package dev.javaquest.career;

import dev.javaquest.career.GameRules.Card;
import dev.javaquest.career.GameRules.ReviewGrade;
import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.QuizSession;
import dev.javaquest.content.Topic;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Turns learning events into XP, streak days, review cards and kudos. */
@Service
public class CareerService {

    public static final int READ_XP = 10;
    public static final int PERFECT_QUIZ_BONUS = 20;
    public static final int NO_HINTS_BONUS = 25;
    public static final int GOOD_REVIEW_XP = 15;
    public static final int REVIEW_QUESTIONS = 3;
    public static final int BOSS_BRONZE_XP = 300;
    public static final int BOSS_TIER_BONUS = 100;
    public static final int FEATURE_XP = 400;

    private final CareerStore store;
    private final ContentCatalog catalog;
    private final Clock clock;

    public CareerService(CareerStore store, ContentCatalog catalog, Clock clock) {
        this.store = store;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional
    public void onLessonRead(Topic topic) {
        award("read", READ_XP, topic, "read:" + topic.id());
    }

    @Transactional
    public void onChecksPassedFirstTime(Topic topic, int hintsUsed, boolean solutionViewed) {
        if (topic.meta().isBoss()) {
            return;
        }
        if (topic.meta().isFeature()) {
            award("feature", solutionViewed ? FEATURE_XP / 2 : FEATURE_XP, topic, "feature:" + topic.id());
            refreshKudos();
            return;
        }
        int xp = topic.meta().xp().code();
        award("code", solutionViewed ? xp / 2 : xp, topic, "code:" + topic.id());
        if (hintsUsed == 0) {
            award("code-no-hints", NO_HINTS_BONUS, topic, "no-hints:" + topic.id());
        }
        refreshKudos();
    }

    @Transactional
    public void onBossTiers(Topic topic, java.util.Set<dev.javaquest.progress.BossTiers.Tier> tiers) {
        for (var tier : tiers) {
            switch (tier) {
                case BRONZE -> award("boss", BOSS_BRONZE_XP, topic, "boss:" + topic.id());
                case SILVER -> award("boss-silver", BOSS_TIER_BONUS, topic, "boss-silver:" + topic.id());
                case GOLD -> award("boss-gold", BOSS_TIER_BONUS, topic, "boss-gold:" + topic.id());
            }
        }
        refreshKudos();
    }

    @Transactional
    public void onQuizGraded(Topic topic, QuizSession.Result result) {
        if (result.passed()) {
            award("quiz", topic.meta().xp().quiz(), topic, "quiz:" + topic.id());
        }
        if (result.scorePercent() == 100) {
            award("quiz-perfect", PERFECT_QUIZ_BONUS, topic, "perfect:" + topic.id());
        }
        int spotted = (int) result.questions().stream().filter(q -> q.correct() && q.type().equals("spot-the-bug")).count();
        if (spotted > 0) {
            store.addSpotBugsCorrect(spotted);
        }
        refreshKudos();
    }

    @Transactional
    public void onMastered(Topic topic) {
        if (topic.hasQuiz() && topic.meta().review() && store.card(topic.id()).isEmpty()) {
            store.saveCard(topic.id(), Card.fresh(today()), null);
        }
        refreshKudos();
    }

    @Transactional
    public ReviewGrade recordReview(Topic topic, QuizSession.Result result) {
        long correct = result.questions().stream().filter(QuizSession.Graded::correct).count();
        ReviewGrade grade = GameRules.reviewGrade((int) correct, result.questions().size());
        Card card = store.card(topic.id()).orElse(Card.fresh(today()));
        store.saveCard(topic.id(), GameRules.schedule(card, grade, today()), grade.name());
        if (grade == ReviewGrade.GOOD) {
            award("review", GOOD_REVIEW_XP, topic, "review:" + topic.id() + ":" + today());
        }
        refreshKudos();
        return grade;
    }

    private void award(String kind, int amount, Topic topic, String dedupeKey) {
        LocalDate today = today();
        if (store.addXp(kind, amount, topic == null ? null : topic.id(), dedupeKey, today, now())) {
            countDayIfEarned(today);
        }
    }

    private void countDayIfEarned(LocalDate today) {
        var streak = store.streak();
        if (today.equals(streak.lastActive()) || store.xpOn(today) < GameRules.DAILY_XP_FOR_STREAK) {
            return;
        }
        var next = GameRules.afterActiveDay(streak, today);
        store.saveStreak(next);
        store.addXp("streak", GameRules.dailyStreakXp(next.current()), null, "streak:" + today, today, now());
    }

    private void refreshKudos() {
        var mastered = store.masteredTopics();
        var stats = new Kudos.Stats(mastered, store.countXp("code") > 0, store.countXp("code-no-hints"),
                store.countXp("quiz-perfect"), store.spotBugsCorrect(), store.idePasses(), store.streak().longest(),
                store.countXp("review"), completeModules(), store.countXp("boss"), store.countXp("boss-gold"));
        var have = store.kudos();
        for (String id : Kudos.earned(stats)) {
            if (!have.contains(id)) {
                store.addKudos(id, now());
            }
        }
    }

    /** Only modules whose incident exists count, so a module never ships before its content is complete. */
    public Map<String, List<String>> completeModules() {
        var modules = new LinkedHashMap<String, List<String>>();
        for (var module : catalog.modules()) {
            var units = catalog.topics().stream().filter(t -> t.meta().moduleId().equals(module.id())).map(Topic::id).toList();
            if (units.contains(module.id() + "/boss")) {
                modules.put(module.id(), units);
            }
        }
        return modules;
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private Instant now() {
        return clock.instant();
    }
}
