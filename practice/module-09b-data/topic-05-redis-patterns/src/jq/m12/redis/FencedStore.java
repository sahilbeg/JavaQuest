package jq.m12.redis;

import java.util.ArrayList;
import java.util.List;

/** A store that refuses writes from a lock holder whose fencing token is older than one it has already seen. */
public final class FencedStore {

    private final List<String> entries = new ArrayList<>();
    private long highestToken = Long.MIN_VALUE;

    /** Keeps the entry and answers true, unless fencingToken is LOWER than the highest token accepted so far. */
    public synchronized boolean write(long fencingToken, String entry) {
        // TODO 19: refuse (false) a token below highestToken. Otherwise remember the token, keep the entry, answer true.
        return false;
    }

    /** The accepted entries, oldest first. */
    public synchronized List<String> entries() {
        return List.copyOf(entries);
    }
}
