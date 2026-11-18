package jq.m12.redis;

import java.util.ArrayList;
import java.util.List;

/** A store that refuses writes from a lock holder whose fencing token is older than one it has already seen. */
public final class FencedStore {

    private final List<String> entries = new ArrayList<>();
    private long highestToken = Long.MIN_VALUE;

    public synchronized boolean write(long fencingToken, String entry) {
        if (fencingToken < highestToken) {
            return false;
        }
        highestToken = fencingToken;
        entries.add(entry);
        return true;
    }

    public synchronized List<String> entries() {
        return List.copyOf(entries);
    }
}
