package dev.cadence.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.cadence.progress.BossTiers.Tier;
import dev.cadence.runner.TestRunResult.TestCaseResult;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class BossTiersTest {

    private static TestCaseResult check(String className, boolean passed) {
        return new TestCaseResult("jq.m00.boss." + className, "a check", passed, "");
    }

    @Test
    void eachTierNeedsAllOfItsOwnChecks() {
        var tests = List.of(check("KioskBronzeTest", true), check("KioskBronzeTest", true),
                check("KioskSilverTest", true), check("KioskSilverTest", false), check("KioskGoldTest", true));
        assertEquals(EnumSet.of(Tier.BRONZE, Tier.GOLD), BossTiers.passed(tests));
    }

    @Test
    void aTierWithNoChecksIsNotPassed() {
        assertEquals(EnumSet.of(Tier.BRONZE), BossTiers.passed(List.of(check("KioskBronzeTest", true))));
    }

    @Test
    void otherTestClassesDoNotCount() {
        assertEquals(EnumSet.noneOf(Tier.class), BossTiers.passed(List.of(check("KioskTest", true))));
    }
}
