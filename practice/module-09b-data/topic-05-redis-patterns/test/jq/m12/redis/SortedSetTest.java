package jq.m12.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SortedSetTest {

    private static final String WEEK = "leaderboard:2026-W47";

    private final MutableClock clock = new MutableClock();
    private final MiniRedis redis = new MiniRedis(clock);

    @Test
    @DisplayName("ZADD answers 1 for a new member and 0 for a score change; ZINCRBY adds; ZSCORE reads the score")
    void zaddAndZscore() {
        assertEquals(1, redis.zadd(WEEK, 12, "rider-7"), "ZADD of a new member should answer 1");
        assertEquals(0, redis.zadd(WEEK, 15, "rider-7"), "ZADD of a member already there should answer 0 and update its score");
        assertEquals(15.0, redis.zscore(WEEK, "rider-7"), "ZSCORE should give the latest score, 15");
        assertNull(redis.zscore(WEEK, "rider-8"), "ZSCORE of a member that is not in the set should be null");
        assertNull(redis.zscore("leaderboard:2026-W46", "rider-7"), "ZSCORE on a missing key should be null");

        assertEquals(1.0, redis.zincrby(WEEK, 1, "rider-8"), "ZINCRBY on a missing member starts from 0: 0 + 1 = 1");
        assertEquals(4.0, redis.zincrby(WEEK, 3, "rider-8"), "ZINCRBY 3 should add 3 to 1 and answer the new score, 4");
        assertEquals(4.0, redis.zscore(WEEK, "rider-8"), "ZSCORE should agree with the last ZINCRBY answer");
    }

    @Test
    @DisplayName("ZREVRANGE lists the highest scores first, with start and stop both included")
    void zrevrangeOrder() {
        redis.zadd(WEEK, 12, "rider-7");
        redis.zadd(WEEK, 30, "rider-2");
        redis.zadd(WEEK, 5, "rider-40");
        redis.zadd(WEEK, 21, "rider-13");

        assertEquals(List.of(new ScoredMember("rider-2", 30), new ScoredMember("rider-13", 21)),
                redis.zrevrange(WEEK, 0, 1), "ZREVRANGE 0 1 should give the top two: rider-2 (30), then rider-13 (21)");
        assertEquals(List.of(new ScoredMember("rider-2", 30), new ScoredMember("rider-13", 21),
                        new ScoredMember("rider-7", 12), new ScoredMember("rider-40", 5)),
                redis.zrevrange(WEEK, 0, -1), "ZREVRANGE 0 -1 should give every member, highest score first");
    }

    @Test
    @DisplayName("ZREVRANGE handles negative indexes, a stop past the end, and ranges that hold nothing")
    void zrevrangeIndexes() {
        redis.zadd(WEEK, 30, "rider-2");
        redis.zadd(WEEK, 21, "rider-13");
        redis.zadd(WEEK, 12, "rider-7");

        assertEquals(List.of(new ScoredMember("rider-7", 12)), redis.zrevrange(WEEK, -1, -1),
                "-1 means the last place, so ZREVRANGE -1 -1 is the lowest score");
        assertEquals(List.of(new ScoredMember("rider-13", 21), new ScoredMember("rider-7", 12)),
                redis.zrevrange(WEEK, 1, 99), "a stop past the end is cut to the last place");
        assertEquals(List.of(), redis.zrevrange(WEEK, 5, 9), "a start past the end gives an empty list");
        assertEquals(List.of(), redis.zrevrange(WEEK, 2, 1), "a start after the stop gives an empty list");
        assertEquals(List.of(), redis.zrevrange("leaderboard:2026-W46", 0, -1), "a missing key gives an empty list");
    }

    @Test
    @DisplayName("Equal scores are ordered by member name, in reverse, as ZREVRANGE does in Redis")
    void tiesReverseLexicographic() {
        redis.zadd(WEEK, 9, "rider-3");
        redis.zadd(WEEK, 9, "rider-9");
        redis.zadd(WEEK, 9, "rider-10");
        redis.zadd(WEEK, 20, "rider-1");

        assertEquals(List.of(new ScoredMember("rider-1", 20), new ScoredMember("rider-9", 9),
                        new ScoredMember("rider-3", 9), new ScoredMember("rider-10", 9)),
                redis.zrevrange(WEEK, 0, -1),
                "ties on 9 go in reverse text order: \"rider-9\" > \"rider-3\" > \"rider-10\" (text compares '9' > '3' > '1', it does not compare numbers)");
    }

    @Test
    @DisplayName("ZREVRANK gives a 0-based place from the top, using the same tie order, or null")
    void zrevrank() {
        redis.zadd(WEEK, 9, "rider-3");
        redis.zadd(WEEK, 9, "rider-9");
        redis.zadd(WEEK, 20, "rider-1");

        assertEquals(0L, redis.zrevrank(WEEK, "rider-1"), "the highest score should have rank 0");
        assertEquals(1L, redis.zrevrank(WEEK, "rider-9"), "rider-9 ties with rider-3 and sorts first in reverse order: rank 1");
        assertEquals(2L, redis.zrevrank(WEEK, "rider-3"), "rider-3 is third: rank 2");
        assertNull(redis.zrevrank(WEEK, "rider-77"), "a member not in the set should give null");
        assertNull(redis.zrevrank("leaderboard:2026-W46", "rider-1"), "a missing key should give null");
    }

    @Test
    @DisplayName("A leaderboard key can expire like any other key")
    void zsetExpires() {
        redis.zincrby(WEEK, 2, "rider-7");
        assertEquals(1, redis.expire(WEEK, 60), "EXPIRE should work on a sorted set too");
        redis.zincrby(WEEK, 1, "rider-7");
        assertEquals(60, redis.ttl(WEEK), "ZINCRBY changes a score but keeps the key's TTL");

        clock.advanceMillis(60_000);
        assertEquals(List.of(), redis.zrevrange(WEEK, 0, -1), "after it expires the leaderboard should be empty");
        assertNull(redis.zscore(WEEK, "rider-7"), "after it expires ZSCORE should be null");
        assertEquals(1.0, redis.zincrby(WEEK, 1, "rider-7"), "a ZINCRBY after expiry starts a fresh set: 0 + 1 = 1");
    }
}
