package dev.javaquest.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProgressRulesTest {

    @Test
    void firstHintIsOpenAndLaterHintsNeedFailedRuns() {
        assertEquals(1, ProgressRules.hintsAvailable(0, 3));
        assertEquals(1, ProgressRules.hintsAvailable(1, 3));
        assertEquals(2, ProgressRules.hintsAvailable(2, 3));
        assertEquals(2, ProgressRules.hintsAvailable(3, 3));
        assertEquals(3, ProgressRules.hintsAvailable(4, 3));
    }

    @Test
    void hintsNeverExceedTheTopicsHintCount() {
        assertEquals(3, ProgressRules.hintsAvailable(50, 3));
        assertEquals(2, ProgressRules.hintsAvailable(50, 2));
    }

    @Test
    void solutionOpensWhenTheChecksPass() {
        assertTrue(ProgressRules.solutionAvailable(true, 0, 3, 0, 0));
    }

    @Test
    void solutionNeedsAllHintsAndOneMoreFailedRun() {
        assertFalse(ProgressRules.solutionAvailable(false, 2, 3, 9, 4));
        assertFalse(ProgressRules.solutionAvailable(false, 3, 3, 4, 4), "no failed run since the last hint");
        assertTrue(ProgressRules.solutionAvailable(false, 3, 3, 5, 4));
    }

    @Test
    void codeTopicNeedsTheQuizAndTheChecks() {
        assertFalse(ProgressRules.mastered(100, 80, false, true, true));
        assertFalse(ProgressRules.mastered(79, 80, true, true, true));
        assertTrue(ProgressRules.mastered(80, 80, true, true, true));
    }

    @Test
    void quizTopicNeedsOnlyTheQuiz() {
        assertTrue(ProgressRules.mastered(80, 80, false, false, true));
    }

    @Test
    void masteryPercentSplitsQuizAndChecksEvenly() {
        assertEquals(0, ProgressRules.masteryPercent(0, 80, false, true, true));
        assertEquals(25, ProgressRules.masteryPercent(40, 80, false, true, true));
        assertEquals(50, ProgressRules.masteryPercent(0, 80, true, true, true));
        assertEquals(100, ProgressRules.masteryPercent(100, 80, true, true, true));
        assertEquals(50, ProgressRules.masteryPercent(40, 80, false, false, true));
    }

    @Test
    void incidentsAndFeaturesNeedOnlyTheirChecks() {
        assertTrue(ProgressRules.mastered(0, 100, true, true, false));
        assertFalse(ProgressRules.mastered(100, 100, false, true, false));
        assertEquals(100, ProgressRules.masteryPercent(0, 100, true, true, false));
        assertEquals(0, ProgressRules.masteryPercent(0, 100, false, true, false));
    }
}
