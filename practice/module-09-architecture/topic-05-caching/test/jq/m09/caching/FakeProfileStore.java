package jq.m09.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** An in-memory stand-in for the slow rider-profile service. It counts loads and can run a step in the middle of a save. */
final class FakeProfileStore implements RiderProfileStore {

    private final Map<String, RiderProfile> rows = new HashMap<>();
    int loads;
    int saves;
    /** Runs at the start of a save, before the new profile is written: another request arriving mid-write. */
    Runnable beforeWrite = () -> {};
    /** Runs at the end of a save, after the new profile is written. */
    Runnable afterWrite = () -> {};

    void seed(RiderProfile profile) {
        rows.put(profile.riderId(), profile);
    }

    @Override
    public Optional<RiderProfile> load(String riderId) {
        loads++;
        return Optional.ofNullable(rows.get(riderId));
    }

    @Override
    public void save(RiderProfile profile) {
        saves++;
        Runnable before = beforeWrite;
        beforeWrite = () -> {};
        before.run();
        rows.put(profile.riderId(), profile);
        Runnable after = afterWrite;
        afterWrite = () -> {};
        after.run();
    }
}
