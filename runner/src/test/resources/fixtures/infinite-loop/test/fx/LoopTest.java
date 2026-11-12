package fx;

import org.junit.jupiter.api.Test;

class LoopTest {
    @Test
    void neverEnds() {
        while (true) {
            Thread.onSpinWait();
        }
    }
}
