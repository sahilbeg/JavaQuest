package fx;

import org.junit.jupiter.api.Test;

class HeapTest {
    @Test
    void allocatesAboutOneHundredMegabytes() {
        long[] big = new long[12_500_000];
        big[0] = 1;
    }
}
