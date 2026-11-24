package dev.cadence.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.cadence.runner.TestRunResult.TestCaseResult;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class BossTiersWebTest {

    @Test
    void webTestFilesNameTheirTier() {
        var tests = List.of(
                new TestCaseResult("test/CartBronze.test.jsx", "adds items", true, ""),
                new TestCaseResult("test/CartSilver.test.js", "keeps the cart", true, ""),
                new TestCaseResult("test/CartGold.test.jsx", "survives a refresh", false, "nope"));
        assertEquals(EnumSet.of(BossTiers.Tier.BRONZE, BossTiers.Tier.SILVER), BossTiers.passed(tests));
    }
}
