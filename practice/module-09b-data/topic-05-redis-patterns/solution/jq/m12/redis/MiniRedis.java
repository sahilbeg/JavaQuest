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

    public synchronized String set(String key, String value) {
        return set(key, value, SetOptions.none());
    }

    public synchronized String set(String key, String value, SetOptions options) {
        removeIfExpired(key);
        if (options.onlyIfAbsent() && data.containsKey(key)) {
            return null;
        }
        data.put(key, value);
        expiresAt.remove(key);
        if (options.pxMillis() > 0) {
            expiresAt.put(key, now() + options.pxMillis());
        }
        return "OK";
    }

    public synchronized String get(String key) {
        return stringAt(key);
    }

    public synchronized long del(String... keys) {
        long removed = 0;
        for (String key : keys) {
            removeIfExpired(key);
            if (data.remove(key) != null) {
                expiresAt.remove(key);
                removed++;
            }
        }
        return removed;
    }

    public synchronized long incr(String key) {
        return incrBy(key, 1);
    }

    public synchronized long incrBy(String key, long increment) {
        String current = stringAt(key);
        long value = 0;
        if (current != null) {
            try {
                value = Long.parseLong(current);
            } catch (NumberFormatException e) {
                throw new RedisException(RedisException.NOT_AN_INTEGER);
            }
        }
        long next;
        try {
            next = Math.addExact(value, increment);
        } catch (ArithmeticException e) {
            throw new RedisException(RedisException.OVERFLOW);
        }
        data.put(key, Long.toString(next));   // keeps any TTL the key already has
        return next;
    }

    // ---------- expiry ----------

    public synchronized long expire(String key, long seconds) {
        removeIfExpired(key);
        if (!data.containsKey(key)) {
            return 0;
        }
        if (seconds <= 0) {
            data.remove(key);
            expiresAt.remove(key);
        } else {
            expiresAt.put(key, now() + seconds * 1000);
        }
        return 1;
    }

    public synchronized long pttl(String key) {
        removeIfExpired(key);
        if (!data.containsKey(key)) {
            return -2;
        }
        Long deadline = expiresAt.get(key);
        return deadline == null ? -1 : deadline - now();
    }

    public synchronized long ttl(String key) {
        long millis = pttl(key);
        return millis < 0 ? millis : (millis + 500) / 1000;
    }

    // ---------- sorted sets ----------

    public synchronized long zadd(String key, double score, String member) {
        Map<String, Double> zset = sortedSetAt(key, true);
        return zset.put(member, score) == null ? 1 : 0;
    }

    public synchronized double zincrby(String key, double increment, String member) {
        Map<String, Double> zset = sortedSetAt(key, true);
        return zset.merge(member, increment, Double::sum);
    }

    public synchronized Double zscore(String key, String member) {
        Map<String, Double> zset = sortedSetAt(key, false);
        return zset == null ? null : zset.get(member);
    }

    public synchronized List<ScoredMember> zrevrange(String key, long start, long stop) {
        List<ScoredMember> ordered = highestFirst(key);
        int size = ordered.size();
        if (start < 0) start += size;
        if (stop < 0) stop += size;
        if (start < 0) start = 0;
        if (start > stop || start >= size) {
            return List.of();
        }
        if (stop >= size) stop = size - 1;
        return List.copyOf(ordered.subList((int) start, (int) stop + 1));
    }

    public synchronized Long zrevrank(String key, String member) {
        List<ScoredMember> ordered = highestFirst(key);
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).member().equals(member)) {
                return (long) i;
            }
        }
        return null;
    }

    // ---------- helpers ----------

    private long now() {
        return clock.millis();
    }

    private void removeIfExpired(String key) {
        Long deadline = expiresAt.get(key);
        if (deadline != null && now() >= deadline) {
            data.remove(key);
            expiresAt.remove(key);
        }
    }

    private String stringAt(String key) {
        removeIfExpired(key);
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String text)) {
            throw new RedisException(RedisException.WRONG_TYPE);
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> sortedSetAt(String key, boolean create) {
        removeIfExpired(key);
        Object value = data.get(key);
        if (value == null) {
            if (!create) {
                return null;
            }
            Map<String, Double> zset = new HashMap<>();
            data.put(key, zset);
            return zset;
        }
        if (!(value instanceof Map)) {
            throw new RedisException(RedisException.WRONG_TYPE);
        }
        return (Map<String, Double>) value;
    }

    private List<ScoredMember> highestFirst(String key) {
        Map<String, Double> zset = sortedSetAt(key, false);
        List<ScoredMember> ordered = new ArrayList<>();
        if (zset == null) {
            return ordered;
        }
        zset.forEach((member, score) -> ordered.add(new ScoredMember(member, score)));
        ordered.sort(Comparator.comparingDouble(ScoredMember::score)
                .thenComparing(ScoredMember::member)
                .reversed());
        return ordered;
    }
}
