package jq.m06.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupsTest {

    private static Map<String, Consumer<GroupDiscount>> allChecks() {
        Map<String, Consumer<GroupDiscount>> checks = new LinkedHashMap<>();
        checks.put("checkSmallGroupsPayFullFare", GroupDiscountChecks::checkSmallGroupsPayFullFare);
        checks.put("checkThreeOrFourGetTwentyPercentOff", GroupDiscountChecks::checkThreeOrFourGetTwentyPercentOff);
        checks.put("checkFiveOrMoreGetThirtyPercentOff", GroupDiscountChecks::checkFiveOrMoreGetThirtyPercentOff);
        checks.put("checkRoundsDownToWholeCents", GroupDiscountChecks::checkRoundsDownToWholeCents);
        checks.put("checkRejectsGroupSizeBelowOne", GroupDiscountChecks::checkRejectsGroupSizeBelowOne);
        return checks;
    }

    private static void assertAllPassOnReference() {
        for (Map.Entry<String, Consumer<GroupDiscount>> check : allChecks().entrySet()) {
            try {
                check.getValue().accept(new ReferenceGroups());
            } catch (Throwable problem) {
                fail(check.getKey() + " failed against the reference discount, which is correct. "
                        + "Your check expects the wrong answer: " + problem);
            }
        }
    }

    private static void assertAllPassOnGroups() {
        for (Map.Entry<String, Consumer<GroupDiscount>> check : allChecks().entrySet()) {
            try {
                check.getValue().accept(new Groups());
            } catch (Throwable problem) {
                fail(check.getKey() + " failed against your Groups class. "
                        + "The check passed against the reference, so fix Groups: " + problem);
            }
        }
    }

    private static boolean caught(GroupDiscount mutant) {
        for (Consumer<GroupDiscount> check : allChecks().values()) {
            try {
                check.accept(mutant);
            } catch (AssertionError problem) {
                return true;
            } catch (RuntimeException problem) {
                // Only a failed assertion counts as catching the mutant.
            }
        }
        return false;
    }

    @Test
    @DisplayName("Your checks pass against a correct discount and against your own Groups")
    void checksPassOnCorrectCode() {
        assertAllPassOnReference();
        assertAllPassOnGroups();
    }

    @Test
    @DisplayName("Your checks catch a discount that gives 20 percent off to a pair")
    void catchesTwentyAtTwo() {
        GroupDiscount mutant = (fare, size) -> {
            if (size < 1) {
                throw new IllegalArgumentException();
            }
            if (size >= 5) {
                return fare * 70 / 100;
            }
            if (size >= 2) {
                return fare * 80 / 100;
            }
            return fare;
        };
        assertTrue(caught(mutant), "your checks did not catch a discount that gives 20 percent off to 2 riders. "
                + "Check that a group of 2 pays the full fare");
    }

    @Test
    @DisplayName("Your checks catch a discount that gives 30 percent off to four riders")
    void catchesThirtyAtFour() {
        GroupDiscount mutant = (fare, size) -> {
            if (size < 1) {
                throw new IllegalArgumentException();
            }
            if (size >= 4) {
                return fare * 70 / 100;
            }
            if (size >= 3) {
                return fare * 80 / 100;
            }
            return fare;
        };
        assertTrue(caught(mutant), "your checks did not catch a discount that gives 30 percent off to 4 riders. "
                + "Check that a group of 4 still gets only 20 percent off");
    }

    @Test
    @DisplayName("Your checks catch a discount that rounds up to the next cent")
    void catchesRoundingUp() {
        GroupDiscount mutant = (fare, size) -> {
            if (size < 1) {
                throw new IllegalArgumentException();
            }
            if (size >= 5) {
                return (fare * 70 + 99) / 100;
            }
            if (size >= 3) {
                return (fare * 80 + 99) / 100;
            }
            return fare;
        };
        assertTrue(caught(mutant), "your checks did not catch a discount that rounds up. "
                + "Use a fare like 1234 cents, where 20 percent off leaves a fraction of a cent");
    }

    @Test
    @DisplayName("Your checks catch a discount that accepts a group of zero or fewer riders")
    void catchesMissingException() {
        GroupDiscount mutant = (fare, size) -> {
            if (size >= 5) {
                return fare * 70 / 100;
            }
            if (size >= 3) {
                return fare * 80 / 100;
            }
            return fare;
        };
        assertTrue(caught(mutant), "your checks did not catch a discount that accepts a group size of 0 or -3. "
                + "Use assertThrows(IllegalArgumentException.class, ...)");
    }

    @Test
    @DisplayName("Groups gives the right discount for every group size")
    void groupsBehaviour() {
        Groups groups = new Groups();
        assertEquals(1500, groups.discountedCents(1500, 1), "1 rider on a 1500 cent fare pays 1500");
        assertEquals(1500, groups.discountedCents(1500, 2), "2 riders on a 1500 cent fare pay 1500 each");
        assertEquals(1200, groups.discountedCents(1500, 3), "3 riders get 20 percent off: 1500 becomes 1200");
        assertEquals(1200, groups.discountedCents(1500, 4), "4 riders get 20 percent off: 1500 becomes 1200");
        assertEquals(1050, groups.discountedCents(1500, 5), "5 riders get 30 percent off: 1500 becomes 1050");
        assertEquals(1050, groups.discountedCents(1500, 9), "9 riders get 30 percent off: 1500 becomes 1050");
        assertEquals(799, groups.discountedCents(999, 3), "80 percent of 999 is 799.2, which rounds down to 799");
        assertEquals(699, groups.discountedCents(999, 5), "70 percent of 999 is 699.3, which rounds down to 699");
        assertThrows(IllegalArgumentException.class, () -> groups.discountedCents(1500, 0),
                "a group size of 0 should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> groups.discountedCents(1500, -1),
                "a group size of -1 should throw IllegalArgumentException");
    }
}
