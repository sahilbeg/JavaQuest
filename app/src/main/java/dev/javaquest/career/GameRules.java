package dev.javaquest.career;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** The game rules from docs/GAMIFICATION.md, free of storage so each one is tested directly. */
public final class GameRules {

    public static final List<String> JOB_TITLES = List.of("Intern", "Junior Developer", "Developer", "Software Engineer",
            "Senior Engineer", "Staff Engineer", "Architect", "Principal Engineer");
    public static final int LEVELS_PER_TITLE = 5;
    public static final int DAILY_XP_FOR_STREAK = 10;
    public static final int FREEZE_EVERY_DAYS = 7;
    public static final int MAX_FREEZES = 2;

    private GameRules() {}

    /** Total XP needed to reach a level. Level 1 is free; level 2 needs 100, level 3 needs 300, level 4 needs 600. */
    public static int xpForLevel(int level) {
        int n = level - 1;
        return 100 * n * (n + 1) / 2;
    }

    public static int level(int xp) {
        int level = 1;
        while (xpForLevel(level + 1) <= xp) {
            level++;
        }
        return level;
    }

    public static String jobTitle(int level) {
        return JOB_TITLES.get(Math.min((level - 1) / LEVELS_PER_TITLE, JOB_TITLES.size() - 1));
    }

    public static int dailyStreakXp(int streak) {
        return Math.min(50, 5 * streak);
    }

    public record Streak(int current, int longest, LocalDate lastActive, int freezes) {}

    /** Applies a newly active day. Missed days are covered by freezes when there are enough of them. */
    public static Streak afterActiveDay(Streak state, LocalDate today) {
        if (today.equals(state.lastActive())) {
            return state;
        }
        int current;
        int freezes = state.freezes();
        if (state.lastActive() == null) {
            current = 1;
        } else {
            long missed = ChronoUnit.DAYS.between(state.lastActive(), today) - 1;
            if (missed == 0) {
                current = state.current() + 1;
            } else if (missed > 0 && missed <= freezes) {
                freezes -= (int) missed;
                current = state.current() + 1;
            } else {
                current = 1;
            }
        }
        if (current % FREEZE_EVERY_DAYS == 0) {
            freezes = Math.min(MAX_FREEZES, freezes + 1);
        }
        return new Streak(current, Math.max(state.longest(), current), today, freezes);
    }

    /** The streak as it stands today, before today counts. It is zero once the gap is more than the freezes cover. */
    public static int currentStreak(Streak state, LocalDate today) {
        if (state.lastActive() == null) {
            return 0;
        }
        long missed = ChronoUnit.DAYS.between(state.lastActive(), today) - 1;
        return missed <= state.freezes() ? state.current() : 0;
    }

    public record Card(double ease, int intervalDays, int repetitions, LocalDate dueOn) {
        public static Card fresh(LocalDate today) {
            return new Card(2.5, 1, 0, today.plusDays(1));
        }
    }

    public enum ReviewGrade { GOOD, HARD, AGAIN }

    public static ReviewGrade reviewGrade(int correct, int total) {
        if (correct == total) {
            return ReviewGrade.GOOD;
        }
        return correct == total - 1 ? ReviewGrade.HARD : ReviewGrade.AGAIN;
    }

    /** SM-2: GOOD is quality 5, HARD is 3, AGAIN is 1 and starts the card over. */
    public static Card schedule(Card card, ReviewGrade grade, LocalDate today) {
        int quality = switch (grade) {
            case GOOD -> 5;
            case HARD -> 3;
            case AGAIN -> 1;
        };
        double ease = Math.max(1.3, card.ease() + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (quality < 3) {
            return new Card(ease, 1, 0, today.plusDays(1));
        }
        int repetitions = card.repetitions() + 1;
        int interval = repetitions == 1 ? 1 : repetitions == 2 ? 6 : (int) Math.round(card.intervalDays() * ease);
        return new Card(ease, interval, repetitions, today.plusDays(interval));
    }
}
