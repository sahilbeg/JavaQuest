package dev.javaquest.progress;

/** The progress rules from docs/GAMIFICATION.md, kept free of storage so each one is tested directly. */
public final class ProgressRules {

    public static final int HINT_2_AFTER_FAILED_RUNS = 2;
    public static final int HINT_3_AFTER_FAILED_RUNS = 4;

    private ProgressRules() {}

    public static int hintsAvailable(int failedRuns, int hintCount) {
        int unlocked = failedRuns >= HINT_3_AFTER_FAILED_RUNS ? 3 : failedRuns >= HINT_2_AFTER_FAILED_RUNS ? 2 : 1;
        return Math.min(unlocked, hintCount);
    }

    public static boolean solutionAvailable(boolean testsPassed, int hintsUsed, int hintCount,
                                            int failedRuns, int failedRunsAtLastHint) {
        return testsPassed || (hintsUsed >= hintCount && failedRuns > failedRunsAtLastHint);
    }

    public static boolean mastered(int quizBest, int passPercent, boolean testsPassed, boolean codeTopic) {
        return quizBest >= passPercent && (testsPassed || !codeTopic);
    }

    public static int masteryPercent(int quizBest, int passPercent, boolean testsPassed, boolean codeTopic) {
        int quizPart = Math.min(100, quizBest * 100 / passPercent);
        if (!codeTopic) {
            return quizPart;
        }
        return quizPart / 2 + (testsPassed ? 50 : 0);
    }
}
