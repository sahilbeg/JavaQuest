package dev.javaquest.progress;

/**
 * How the learner wants to pace the course.
 *
 * @param dailyLimitOn when true, at most {@code dailyLimit} new tickets can be completed each day; the next one
 *                     opens the following morning. Tickets already completed stay open for revisiting.
 * @param dailyLimit   new tickets per day, from {@link #MIN_LIMIT} to {@link #MAX_LIMIT}
 */
public record StudySettings(boolean dailyLimitOn, int dailyLimit) {

    public static final int MIN_LIMIT = 1;
    public static final int MAX_LIMIT = 20;

    public void validate() {
        if (dailyLimit < MIN_LIMIT || dailyLimit > MAX_LIMIT) {
            throw new IllegalArgumentException("Choose between " + MIN_LIMIT + " and " + MAX_LIMIT + " tickets a day.");
        }
    }
}
