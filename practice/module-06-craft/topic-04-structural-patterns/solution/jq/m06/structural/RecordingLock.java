package jq.m06.structural;

import java.util.ArrayList;
import java.util.List;

public class RecordingLock implements Lock {

    private final Lock inner;
    private final List<String> history = new ArrayList<>();

    public RecordingLock(Lock inner) {
        this.inner = inner;
    }

    @Override
    public boolean open(String bikeId) {
        boolean ok = inner.open(bikeId);
        history.add("open " + bikeId + ": " + ok);
        return ok;
    }

    @Override
    public boolean close(String bikeId) {
        boolean ok = inner.close(bikeId);
        history.add("close " + bikeId + ": " + ok);
        return ok;
    }

    public List<String> history() {
        return List.copyOf(history);
    }
}
