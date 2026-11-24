package dev.cadence.progress;

import dev.cadence.runner.TestRunResult.TestCaseResult;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * An incident's checks are split into tiers by test name: Java test classes KioskBronzeTest, KioskSilverTest and
 * KioskGoldTest, or web test files CartBronze.test.jsx, CartSilver.test.jsx and CartGold.test.jsx.
 */
public final class BossTiers {

    public enum Tier {
        BRONZE, SILVER, GOLD;

        String classSuffix() {
            return word() + "Test";
        }

        private String word() {
            return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT);
        }

        boolean owns(String className) {
            return className.endsWith(classSuffix()) || className.matches(".*" + word() + "\\.test\\.jsx?$");
        }
    }

    private BossTiers() {}

    /** A tier is passed when it has at least one check and every one of its checks passes. */
    public static EnumSet<Tier> passed(List<TestCaseResult> tests) {
        var passed = EnumSet.noneOf(Tier.class);
        for (Tier tier : Tier.values()) {
            var own = tests.stream().filter(t -> tier.owns(t.className())).toList();
            if (!own.isEmpty() && own.stream().allMatch(TestCaseResult::passed)) {
                passed.add(tier);
            }
        }
        return passed;
    }
}
