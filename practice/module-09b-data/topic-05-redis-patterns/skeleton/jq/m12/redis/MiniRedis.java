package jq.m12.redis;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A tiny, in-memory Redis with a handful of commands and Redis's real rules.
 * Every method is synchronized, so like real Redis, one command runs at a time and each command is atomic.
 */
public final class MiniRedis {

    private final Clock clock;
    private final Map<String, Object> data = new HashMap<>();      // a String, or a Map<String, Double> for a sorted set
    private final Map<String, Long> expiresAt = new HashMap<>();   // key -> deadline in epoch milliseconds

    public MiniRedis(Clock clock) {
        this.clock = clock;
    }

    /** Runs several commands as one step, the way Redis runs a Lua script. Given, finished. */
    public synchronized <T> T atomically(Function<MiniRedis, T> script) {
        return script.apply(this);
    }

    // ---------- strings ----------

    /** SET key value. Given, finished. */
    public synchronized String set(String key, String value) {
        return set(key, value, SetOptions.none());
    }

    /** SET key value [NX] [PX ms]: "OK", or null when NX finds the key already there. */
    public synchronized String set(String key, String value, SetOptions options) {
        // TODO 2: removeIfExpired(key) first. With options.onlyIfAbsent() and the key present, return null.
        //         Otherwise store the value (any old type is replaced), forget any old TTL,
        //         and when options.pxMillis() > 0 set the deadline to now() + pxMillis. Return "OK".
        return null;
    }

    /** GET key: the value, or null for a missing key. WRONGTYPE for a sorted set. */
    public synchronized String get(String key) {
        // TODO 4: use stringAt(key)
        return null;
    }

    /** DEL key...: how many of the keys existed and were removed. */
    public synchronized long del(String... keys) {
        // TODO 5: for each key, removeIfExpired, then remove it and its TTL. Count only keys that were there.
        return 0;
    }

    /** INCR key. Given, finished: it calls incrBy. */
    public synchronized long incr(String key) {
        return incrBy(key, 1);
    }

    /** INCRBY key increment: the new value. A missing key counts as 0. */
    public synchronized long incrBy(String key, long increment) {
        // TODO 6: read the string with stringAt (missing = 0). Text that Long.parseLong rejects throws
        //         new RedisException(RedisException.NOT_AN_INTEGER). Add with Math.addExact; on ArithmeticException
        //         throw new RedisException(RedisException.OVERFLOW). Store the result as text. Keep the key's TTL.
        return 0;
    }

    // ---------- expiry ----------

    /** EXPIRE key seconds: 1 if the key exists, 0 if not. 0 or fewer seconds deletes the key. */
    public synchronized long expire(String key, long seconds) {
        // TODO 7: removeIfExpired first. Missing key: 0. seconds <= 0: delete the key and answer 1.
        //         Otherwise the deadline becomes now() + seconds * 1000; answer 1.
        return 0;
    }

    /** PTTL key: milliseconds left, -1 for a key with no expiry, -2 for a missing key. */
    public synchronized long pttl(String key) {
        // TODO 8: removeIfExpired first, then apply the three rules
        return 0;
    }

    /** TTL key: like PTTL, but in seconds, rounded to the nearest second (halves round up). */
    public synchronized long ttl(String key) {
        // TODO 9: use pttl. Keep -1 and -2 as they are; otherwise (millis + 500) / 1000.
        return 0;
    }

    // ---------- sorted sets ----------

    /** ZADD key score member: 1 if the member is new, 0 if only its score changed. */
    public synchronized long zadd(String key, double score, String member) {
        // TODO 11: sortedSetAt(key, true), then put the score
        return 0;
    }

    /** ZINCRBY key increment member: the member's new score. A missing member starts at 0. */
    public synchronized double zincrby(String key, double increment, String member) {
        // TODO 12
        return 0;
    }

    /** ZSCORE key member: the score, or null when the key or the member is missing. */
    public synchronized Double zscore(String key, String member) {
        // TODO 13: sortedSetAt(key, false) gives null for a missing key
        return null;
    }

    /** ZREVRANGE key start stop WITHSCORES: places start to stop, both included, highest score first. */
    public synchronized List<ScoredMember> zrevrange(String key, long start, long stop) {
        // TODO 15: take highestFirst(key). A negative index counts from the end (-1 = last place).
        //          After that, a start below 0 becomes 0, and a stop past the end becomes the last place.
        //          If start > stop or start is past the end, return an empty list.
        return List.of();
    }

    /** ZREVRANK key member: the 0-based place from the top, or null when the key or member is missing. */
    public synchronized Long zrevrank(String key, String member) {
        // TODO 16: find the member's index in highestFirst(key)
        return null;
    }

    // ---------- helpers ----------

    private long now() {
        return clock.millis();
    }

    /** Deletes the key and its TTL if its deadline has come: a key is gone once now() >= its deadline. */
    private void removeIfExpired(String key) {
        // TODO 1
    }

    /** The string at key, or null if missing. Throws WRONGTYPE if the key holds something else. */
    private String stringAt(String key) {
        // TODO 3: removeIfExpired, then look at data.get(key). Not a String? throw new RedisException(RedisException.WRONG_TYPE).
        return null;
    }

    /** The sorted set at key. Missing: a new empty one when create is true, else null. WRONGTYPE for a string. */
    @SuppressWarnings("unchecked")
    private Map<String, Double> sortedSetAt(String key, boolean create) {
        // TODO 10: removeIfExpired first. A sorted set is stored as a Map<String, Double> from member to score.
        return null;
    }

    /** Every member with its score: highest score first, and equal scores in REVERSE text order of the member. */
    private List<ScoredMember> highestFirst(String key) {
        List<ScoredMember> ordered = new ArrayList<>();
        // TODO 14: fill ordered from sortedSetAt(key, false) (it may be null), then sort it.
        //          Comparator.comparingDouble(...).thenComparing(...).reversed() reverses BOTH parts.
        return ordered;
    }
}
