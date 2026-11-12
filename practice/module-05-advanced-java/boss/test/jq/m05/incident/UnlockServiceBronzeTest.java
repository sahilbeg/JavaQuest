package jq.m05.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class UnlockServiceBronzeTest {

    @Test
    @DisplayName("Bronze: only one of 50 riders unlocks the same bike")
    void oneWinner() throws InterruptedException {
        for (int round = 1; round <= 10; round++) {
            UnlockService service = new UnlockService();
            AtomicInteger winners = new AtomicInteger();
            List<Runnable> riders = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                String rider = "rider-" + i;
                riders.add(() -> {
                    if (service.unlock("B-1042", rider)) {
                        winners.incrementAndGet();
                    }
                });
            }
            Crowd.runTogether(riders);
            assertEquals(1, winners.get(), "round " + round
                    + ": more than one rider unlocked B-1042; the check and the put must be one atomic step");
        }
    }

    @Test
    @DisplayName("Bronze: different bikes can all be unlocked")
    void differentBikes() {
        UnlockService service = new UnlockService();
        assertEquals(true, service.unlock("B-1042", "ana"), "B-1042 is free");
        assertEquals(true, service.unlock("B-2001", "ben"), "B-2001 is free");
        assertEquals(false, service.unlock("B-1042", "cai"), "B-1042 is already taken by ana");
    }
}
