package jq.m05.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class UnlockServiceGoldTest {

    @Test
    @DisplayName("Gold: moving bikes both ways never freezes and loses no bike")
    void noDeadlock() throws InterruptedException {
        Station riverside = new Station("Riverside", 12);
        Station harbor = new Station("Harbor", 12);
        Runnable toHarbor = () -> {
            for (int i = 0; i < 200; i++) {
                UnlockService.moveBike(riverside, harbor);
            }
        };
        Runnable toRiverside = () -> {
            for (int i = 0; i < 200; i++) {
                UnlockService.moveBike(harbor, riverside);
            }
        };
        Crowd.runTogether(List.of(toHarbor, toRiverside));
        assertEquals(24, riverside.bikes() + harbor.bikes(), "bikes are moved, never created or lost");
    }
}
