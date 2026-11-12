package jq.m05.completablefuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class UnlockFlowTest {

    @Test
    @DisplayName("unlock combines the lock and the receipt into one line")
    void unlockCombines() {
        assertEquals("released B-1042 | receipt for P-r1", UnlockFlow.unlock("r1", "B-1042").join(),
                "unlock(\"r1\", \"B-1042\") should give \"released B-1042 | receipt for P-r1\": the lock line, \" | \", then sendReceipt of the payment id");
    }

    @Test
    @DisplayName("unlock fails when the card is declined")
    void unlockFailsForBlockedRider() {
        assertThrows(CompletionException.class, () -> UnlockFlow.unlock("BLOCKED", "B-1042").join(),
                "unlock for rider BLOCKED should complete exceptionally, because charge throws. Call charge inside supplyAsync");
    }

    @Test
    @DisplayName("unlockOrFallback returns a fallback line with the real message")
    void fallbackMessage() {
        assertEquals("unlock failed: card declined", UnlockFlow.unlockOrFallback("BLOCKED", "B-1042").join(),
                "use exceptionally and the message of getCause(), so the line is \"unlock failed: card declined\"");
    }

    @Test
    @DisplayName("unlockOrFallback passes a normal unlock through unchanged")
    void fallbackPassesSuccess() {
        assertEquals("released B-1050 | receipt for P-r7", UnlockFlow.unlockOrFallback("r7", "B-1050").join(),
                "when charge works, unlockOrFallback should return the same line as unlock");
    }

    @Test
    @DisplayName("unlockAll unlocks every rider and keeps the input order")
    void unlockAllInOrder() {
        List<String> result = UnlockFlow.unlockAll(List.of("r1", "BLOCKED", "r3"), "B-104");
        assertEquals(List.of(
                        "released B-1041 | receipt for P-r1",
                        "unlock failed: card declined",
                        "released B-1043 | receipt for P-r3"),
                result,
                "rider i gets bike bikeIdPrefix + (i + 1). Use unlockOrFallback, wait with allOf, then join each future in list order");
    }
}
