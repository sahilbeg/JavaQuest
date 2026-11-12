package jq.m06.junit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FareChecksTest {

    private static final String[] NAMES = {
        "checkBasicClassic", "checkMembershipFreeMinutes", "checkAfterFreeMinutes",
        "checkEbikeAndCargo", "checkRejectsNegativeMinutes"
    };

    @SuppressWarnings("unchecked")
    private static final Consumer<FareCalculator>[] CHECKS = new Consumer[] {
        (Consumer<FareCalculator>) FareChecks::checkBasicClassic,
        (Consumer<FareCalculator>) FareChecks::checkMembershipFreeMinutes,
        (Consumer<FareCalculator>) FareChecks::checkAfterFreeMinutes,
        (Consumer<FareCalculator>) FareChecks::checkEbikeAndCargo,
        (Consumer<FareCalculator>) FareChecks::checkRejectsNegativeMinutes
    };

    private static boolean anyCheckCatches(FareCalculator mutant) {
        for (Consumer<FareCalculator> check : CHECKS) {
            try {
                check.accept(mutant);
            } catch (AssertionError caught) {
                return true;
            } catch (RuntimeException ignored) {
                // A crash is not a failed assertion, so it does not count as catching the bug.
            }
        }
        return false;
    }

    @Test
    @DisplayName("Every check passes against the correct StandardFare")
    void allChecksPassOnCorrectFare() {
        FareCalculator correct = new StandardFare();
        for (int i = 0; i < CHECKS.length; i++) {
            try {
                CHECKS[i].accept(correct);
            } catch (Throwable t) {
                fail(NAMES[i] + " failed against the correct StandardFare, so the check itself is wrong: "
                        + t.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Your checks catch a fare that forgets the unlock fee for BASIC riders")
    void catchesMissingUnlockFee() {
        assertTrue(anyCheckCatches(new ForgetsUnlockFee()),
                "your checks did not catch a fare that forgets the 100 cent unlock fee for BASIC riders");
    }

    @Test
    @DisplayName("Your checks catch PLUS free minutes that stop one minute early")
    void catchesPlusOffByOne() {
        assertTrue(anyCheckCatches(new PlusFreeMinutesOffByOne()),
                "your checks did not catch a fare that gives PLUS riders only 29 free minutes instead of 30");
    }

    @Test
    @DisplayName("Your checks catch an e-bike priced like a classic bike")
    void catchesEbikeAsClassic() {
        assertTrue(anyCheckCatches(new EbikePricedAsClassic()),
                "your checks did not catch a fare that charges e-bikes 15 cents a minute instead of 30");
    }

    @Test
    @DisplayName("Your checks catch PRO riders treated like PLUS riders")
    void catchesProLikePlus() {
        assertTrue(anyCheckCatches(new ProTreatedLikePlus()),
                "your checks did not catch a fare that gives PRO riders 30 free minutes instead of 60");
    }

    @Test
    @DisplayName("Your checks catch a fare that accepts negative minutes")
    void catchesNegativeMinutes() {
        assertTrue(anyCheckCatches(new AcceptsNegativeMinutes()),
                "your checks did not catch a fare that returns a price for -1 minutes instead of throwing");
    }

    private abstract static class Mutant implements FareCalculator {
        long unlockFee = 100;
        int plusFree = 30;
        int proFree = 60;
        long ebikeRate = 30;
        boolean rejectNegative = true;

        @Override
        public long fareCents(String type, int minutes, String membership) {
            if (rejectNegative && minutes < 0) {
                throw new IllegalArgumentException("minutes cannot be negative");
            }
            long perMinute = 15;
            if (type.equals("ebike")) {
                perMinute = ebikeRate;
            } else if (type.equals("cargo")) {
                perMinute = 25;
            }
            long fee = 0;
            int free = 0;
            if (membership.equals("BASIC")) {
                fee = unlockFee;
            } else if (membership.equals("PLUS")) {
                free = plusFree;
            } else {
                free = proFree;
            }
            return fee + Math.max(0, minutes - free) * perMinute;
        }
    }

    private static class ForgetsUnlockFee extends Mutant {
        ForgetsUnlockFee() { unlockFee = 0; }
    }

    private static class PlusFreeMinutesOffByOne extends Mutant {
        PlusFreeMinutesOffByOne() { plusFree = 29; }
    }

    private static class EbikePricedAsClassic extends Mutant {
        EbikePricedAsClassic() { ebikeRate = 15; }
    }

    private static class ProTreatedLikePlus extends Mutant {
        ProTreatedLikePlus() { proFree = 30; }
    }

    private static class AcceptsNegativeMinutes extends Mutant {
        AcceptsNegativeMinutes() { rejectNegative = false; }
    }
}
