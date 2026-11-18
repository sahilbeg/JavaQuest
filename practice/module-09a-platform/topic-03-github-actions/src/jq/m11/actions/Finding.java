package jq.m11.actions;

/**
 * One problem the audit found.
 *
 * @param rule    the rule id, such as {@code UNPINNED_ACTION}
 * @param where   where it is: {@code workflow}, {@code jobs.build} or {@code jobs.build.steps[2]}
 * @param message one sentence for the person reading the pull request
 */
public record Finding(String rule, String where, String message) {

    @Override
    public String toString() {
        return rule + " at " + where + ": " + message;
    }
}
