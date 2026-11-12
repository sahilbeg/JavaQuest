package jq.m06.structural;

import java.util.ArrayList;
import java.util.List;

public class RecordingLock implements Lock {

    // TODO 7: keep the wrapped Lock and a List<String> history in private final fields.
    public RecordingLock(Lock inner) {
    }

    @Override
    public boolean open(String bikeId) {
        // TODO 8: open the inner lock, add "open B-1042: true" (with the real id and result), return the result.
        return false;
    }

    @Override
    public boolean close(String bikeId) {
        // TODO 9: same as open, with "close B-1042: true".
        return false;
    }

    public List<String> history() {
        // TODO 10: return the history.
        return new ArrayList<>();
    }
}
