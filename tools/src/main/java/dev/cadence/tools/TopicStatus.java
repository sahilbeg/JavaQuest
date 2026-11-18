package dev.cadence.tools;

import java.util.List;

/** The build_state.json status ladder. A topic only moves up it. */
public final class TopicStatus {

    public static final List<String> LADDER =
            List.of("planned", "content_written", "practice_written", "tests_verified", "ui_wired", "done");

    private TopicStatus() {}

    public static String afterVerification(String current, boolean verified) {
        int rank = LADDER.indexOf(current);
        if (rank < 0) {
            throw new IllegalArgumentException("Unknown status: " + current);
        }
        if (!verified) {
            return rank >= LADDER.indexOf("tests_verified") ? "practice_written" : current;
        }
        return rank < LADDER.indexOf("tests_verified") ? "tests_verified" : current;
    }
}
