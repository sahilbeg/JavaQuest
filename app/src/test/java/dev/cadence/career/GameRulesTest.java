package dev.cadence.career;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.cadence.career.GameRules.Card;
import dev.cadence.career.GameRules.ReviewGrade;
import dev.cadence.career.GameRules.Streak;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class GameRulesTest {

    private static final LocalDate MON = LocalDate.of(2026, 11, 9);

    @Test
    void levelThresholdsFollowTheTriangleNumbers() {
        assertEquals(1, GameRules.level(0));
        assertEquals(1, GameRules.level(99));
        assertEquals(2, GameRules.level(100));
        assertEquals(2, GameRules.level(299));
        assertEquals(3, GameRules.level(300));
        assertEquals(4, GameRules.level(600));
        assertEquals(600, GameRules.xpForLevel(4));
    }

    @Test
    void everyFiveLevelsIsAPromotion() {
        assertEquals("Intern", GameRules.jobTitle(1));
        assertEquals("Intern", GameRules.jobTitle(5));
        assertEquals("Junior Developer", GameRules.jobTitle(6));
        assertEquals("Principal Engineer", GameRules.jobTitle(36));
        assertEquals("Principal Engineer", GameRules.jobTitle(99));
    }

    @Test
    void streakBonusIsCapped() {
        assertEquals(5, GameRules.dailyStreakXp(1));
        assertEquals(50, GameRules.dailyStreakXp(10));
        assertEquals(50, GameRules.dailyStreakXp(40));
    }

    @Test
    void consecutiveDaysGrowTheStreakAndTheSameDayCountsOnce() {
        var s = GameRules.afterActiveDay(new Streak(0, 0, null, 0), MON);
        s = GameRules.afterActiveDay(s, MON);
        s = GameRules.afterActiveDay(s, MON.plusDays(1));
        assertEquals(new Streak(2, 2, MON.plusDays(1), 0), s);
    }

    @Test
    void aMissedDayWithoutAFreezeResetsTheStreak() {
        var s = GameRules.afterActiveDay(new Streak(3, 3, MON, 0), MON.plusDays(2));
        assertEquals(new Streak(1, 3, MON.plusDays(2), 0), s);
    }

    @Test
    void sevenDayStreakEarnsAFreezeThatCoversOneMissedDay() {
        var s = new Streak(6, 6, MON, 0);
        s = GameRules.afterActiveDay(s, MON.plusDays(1));
        assertEquals(1, s.freezes());
        s = GameRules.afterActiveDay(s, MON.plusDays(3));
        assertEquals(new Streak(8, 8, MON.plusDays(3), 0), s);
    }

    @Test
    void freezesNeverExceedTwo() {
        var s = GameRules.afterActiveDay(new Streak(20, 20, MON, 2), MON.plusDays(1));
        assertEquals(2, s.freezes());
    }

    @Test
    void currentStreakDropsToZeroOnceTheGapIsTooLong() {
        var s = new Streak(5, 5, MON, 1);
        assertEquals(5, GameRules.currentStreak(s, MON.plusDays(1)));
        assertEquals(5, GameRules.currentStreak(s, MON.plusDays(2)));
        assertEquals(0, GameRules.currentStreak(s, MON.plusDays(3)));
    }

    @Test
    void reviewGradeComesFromCorrectAnswers() {
        assertEquals(ReviewGrade.GOOD, GameRules.reviewGrade(3, 3));
        assertEquals(ReviewGrade.HARD, GameRules.reviewGrade(2, 3));
        assertEquals(ReviewGrade.AGAIN, GameRules.reviewGrade(1, 3));
    }

    @Test
    void goodReviewsSpreadOutAndAgainStartsOver() {
        var card = Card.fresh(MON);
        card = GameRules.schedule(card, ReviewGrade.GOOD, MON);
        assertEquals(1, card.intervalDays());
        card = GameRules.schedule(card, ReviewGrade.GOOD, MON);
        assertEquals(6, card.intervalDays());
        card = GameRules.schedule(card, ReviewGrade.GOOD, MON);
        assertEquals(17, card.intervalDays());
        assertEquals(MON.plusDays(17), card.dueOn());
        card = GameRules.schedule(card, ReviewGrade.AGAIN, MON);
        assertEquals(1, card.intervalDays());
        assertEquals(0, card.repetitions());
    }

    @Test
    void easeNeverFallsBelowTheFloor() {
        var card = Card.fresh(MON);
        for (int i = 0; i < 20; i++) {
            card = GameRules.schedule(card, ReviewGrade.AGAIN, MON);
        }
        assertEquals(1.3, card.ease(), 1e-9);
    }
}
