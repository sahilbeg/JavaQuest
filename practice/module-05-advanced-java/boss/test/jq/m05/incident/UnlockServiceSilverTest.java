package jq.m05.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class UnlockServiceSilverTest {

    @Test
    @DisplayName("Silver: no payment is lost under load")
    void exactCount() throws InterruptedException {
        UnlockService service = new UnlockService();
        List<Runnable> kiosks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            kiosks.add(() -> {
                for (int n = 0; n < 10_000; n++) {
                    service.recordPayment();
                }
            });
        }
        Crowd.runTogether(kiosks);
        assertEquals(80_000, service.payments(),
                "8 kiosks x 10,000 payments; a lower number means payments++ lost updates between threads");
    }
}
