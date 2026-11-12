package dev.javaquest.career;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Kudos are badges given by teammates. Each rule reads only the stats passed in. */
public final class Kudos {

    public record Definition(String id, String title, String from, String message, String howToEarn) {}

    public record Stats(Set<String> masteredTopics, boolean anyChecksPassed, int codePassesWithoutHints,
                        int perfectQuizzes, int spotBugsCorrect, int idePasses, int longestStreak, int goodReviews,
                        Map<String, List<String>> unitsByModule) {}

    public static final String HELLO_WORLD_TOPIC = "module-00-foundations/topic-05-hello-world";

    public static final List<Definition> ALL = List.of(
            new Definition("hello-world", "Hello, World", "maya", "Your laptop builds Java. Welcome to the team!", "Close SPK-0005"),
            new Definition("first-green", "First green checks", "maya", "First time every check went green. Remember this feeling.", "Pass any coding checks"),
            new Definition("no-hints", "No hints needed", "maya", "Ten fixes without a single hint. Impressive.", "Pass 10 coding tickets without hints"),
            new Definition("perfectionist", "Perfectionist", "priya", "Ten perfect onboarding checks!", "Score 100% on 10 checks"),
            new Definition("bug-hunter", "Bug hunter", "sam", "Twenty bugs spotted. Riders thank you.", "Spot 20 bugs in onboarding checks"),
            new Definition("ide-native", "IDE native", "theo", "Ten fixes straight from your IDE. You work like a pro.", "Pass 10 tickets by saving in your IDE"),
            new Definition("on-fire", "On fire", "priya", "Seven days in a row!", "Reach a 7-day streak"),
            new Definition("unstoppable", "Unstoppable", "theo", "Thirty days in a row. Nothing stops you.", "Reach a 30-day streak"),
            new Definition("memory-palace", "Memory palace", "maya", "Fifty good reviews. It's sticking.", "Get 50 good reviews"));

    private Kudos() {}

    public static String projectShippedId(String moduleId) {
        return "shipped-" + moduleId;
    }

    public static Set<String> earned(Stats stats) {
        var earned = new LinkedHashSet<String>();
        if (stats.masteredTopics().contains(HELLO_WORLD_TOPIC)) earned.add("hello-world");
        if (stats.anyChecksPassed()) earned.add("first-green");
        if (stats.codePassesWithoutHints() >= 10) earned.add("no-hints");
        if (stats.perfectQuizzes() >= 10) earned.add("perfectionist");
        if (stats.spotBugsCorrect() >= 20) earned.add("bug-hunter");
        if (stats.idePasses() >= 10) earned.add("ide-native");
        if (stats.longestStreak() >= 7) earned.add("on-fire");
        if (stats.longestStreak() >= 30) earned.add("unstoppable");
        if (stats.goodReviews() >= 50) earned.add("memory-palace");
        stats.unitsByModule().forEach((module, units) -> {
            if (!units.isEmpty() && stats.masteredTopics().containsAll(units)) {
                earned.add(projectShippedId(module));
            }
        });
        return earned;
    }
}
