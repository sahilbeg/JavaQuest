package jq.m09.resilience;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/** A "random" source that always picks the largest allowed value, and writes down every bound it was asked for. */
final class FakeRandom implements RandomGenerator {

    final List<Long> bounds = new ArrayList<>();

    @Override
    public long nextLong() {
        throw new AssertionError("call random.nextLong(ceiling + 1), not random.nextLong() with no bound");
    }

    @Override
    public long nextLong(long bound) {
        bounds.add(bound);
        return bound - 1;
    }
}
