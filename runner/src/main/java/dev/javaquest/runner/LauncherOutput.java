package dev.javaquest.runner;

import java.util.regex.Pattern;

/** The console launcher prints its own summary after the tests finish; learners only need their program's output. */
public final class LauncherOutput {

    private static final Pattern SUMMARY_START =
            Pattern.compile("^(Failures \\(\\d+\\):|Test run finished after \\d+ ms)$", Pattern.MULTILINE);

    private LauncherOutput() {}

    public static String withoutSummary(String output) {
        var matcher = SUMMARY_START.matcher(output);
        return matcher.find() ? output.substring(0, matcher.start()).stripTrailing() : output;
    }
}
