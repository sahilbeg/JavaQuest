package fx;

import org.junit.jupiter.api.Test;

class MemoryTest {
    @Test
    void allocatesTooMuch() {
        long[] big = new long[200_000_000];
        big[0] = 1;
    }
}
