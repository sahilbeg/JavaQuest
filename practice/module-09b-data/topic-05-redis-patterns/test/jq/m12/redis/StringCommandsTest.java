package jq.m12.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringCommandsTest {

    private final MutableClock clock = new MutableClock();
    private final MiniRedis redis = new MiniRedis(clock);

    @Test
    @DisplayName("SET answers OK, GET reads the value back, and GET of a missing key is null")
    void setAndGet() {
        assertEquals("OK", redis.set("rider:7:home", "harbor"), "SET should answer \"OK\", as Redis does");
        assertEquals("harbor", redis.get("rider:7:home"), "GET should read back the value SET stored");
        assertNull(redis.get("rider:8:home"), "GET of a key that was never set should be null, Redis's (nil)");

        redis.set("rider:7:home", "uptown");
        assertEquals("uptown", redis.get("rider:7:home"), "a second SET should replace the old value");
    }

    @Test
    @DisplayName("A key set with PX is there 1 ms before its deadline and gone from the deadline on")
    void pxExpiry() {
        redis.set("quote:EBIKE:20", "700", SetOptions.px(1_000));

        clock.advanceMillis(999);
        assertEquals("700", redis.get("quote:EBIKE:20"), "999 ms into a 1000 ms life, the key should still be there");

        clock.advanceMillis(1);
        assertNull(redis.get("quote:EBIKE:20"), "at exactly 1000 ms the key has expired: GET should be null");
        assertEquals(-2, redis.ttl("quote:EBIKE:20"), "an expired key no longer exists, so TTL should be -2");
        assertEquals(0, redis.del("quote:EBIKE:20"), "DEL of an expired key should count 0 keys removed");
    }

    @Test
    @DisplayName("TTL is -2 for a missing key, -1 for a key with no expiry, and rounds to the nearest second")
    void ttlRules() {
        assertEquals(-2, redis.ttl("nobody"), "TTL of a key that does not exist should be -2");
        assertEquals(-2, redis.pttl("nobody"), "PTTL of a key that does not exist should be -2");

        redis.set("rider:7:home", "harbor");
        assertEquals(-1, redis.ttl("rider:7:home"), "TTL of a key with no expiry should be -1");
        assertEquals(-1, redis.pttl("rider:7:home"), "PTTL of a key with no expiry should be -1");

        redis.set("quote", "700", SetOptions.px(2_500));
        assertEquals(2_500, redis.pttl("quote"), "PTTL should give the milliseconds left: 2500");
        assertEquals(3, redis.ttl("quote"), "2500 ms left rounds to 3 seconds: Redis rounds TTL to the nearest second");
        clock.advanceMillis(1_100);
        assertEquals(1_400, redis.pttl("quote"), "after 1100 ms, 1400 ms should be left");
        assertEquals(1, redis.ttl("quote"), "1400 ms left rounds to 1 second");
    }

    @Test
    @DisplayName("SET replaces the value and throws away any old time to live")
    void setClearsTtl() {
        redis.set("rider:7:home", "harbor", SetOptions.px(5_000));
        redis.set("rider:7:home", "uptown");
        assertEquals(-1, redis.ttl("rider:7:home"), "a plain SET should remove the old TTL, so TTL should now be -1");

        clock.advanceMillis(10_000);
        assertEquals("uptown", redis.get("rider:7:home"), "with its TTL gone, the key should still be there 10 s later");
    }

    @Test
    @DisplayName("EXPIRE answers 1 for a key that exists and 0 for a missing one, and 0 seconds deletes the key")
    void expireRules() {
        assertEquals(0, redis.expire("nobody", 60), "EXPIRE on a missing key should answer 0");

        redis.set("rate:rider-7", "3");
        assertEquals(1, redis.expire("rate:rider-7", 60), "EXPIRE on an existing key should answer 1");
        assertEquals(60, redis.ttl("rate:rider-7"), "after EXPIRE 60, TTL should be 60");

        clock.advanceMillis(60_000);
        assertNull(redis.get("rate:rider-7"), "60 s later the key has expired");

        redis.set("old", "x");
        assertEquals(1, redis.expire("old", 0), "EXPIRE with 0 seconds should still answer 1");
        assertNull(redis.get("old"), "EXPIRE with 0 (or a negative number) deletes the key at once");
    }

    @Test
    @DisplayName("INCR starts a missing key at 0, adds 1 atomically, and INCRBY adds any amount")
    void incr() {
        assertEquals(1, redis.incr("rides:today"), "INCR on a missing key treats it as 0, so the answer is 1");
        assertEquals(2, redis.incr("rides:today"), "the second INCR should answer 2");
        assertEquals(12, redis.incrBy("rides:today", 10), "INCRBY 10 should add 10 to 2");
        assertEquals(7, redis.incrBy("rides:today", -5), "INCRBY with a negative number should subtract");
        assertEquals("7", redis.get("rides:today"), "a counter is stored as a string, so GET should give \"7\"");

        redis.set("big", Long.toString(Long.MAX_VALUE));
        RedisException overflow = assertThrows(RedisException.class, () -> redis.incr("big"),
                "INCR past Long.MAX_VALUE should throw a RedisException, not wrap round to a negative number");
        assertEquals(RedisException.OVERFLOW, overflow.getMessage(), "the overflow error should use RedisException.OVERFLOW");
    }

    @Test
    @DisplayName("INCR keeps the key's TTL, the way a rate-limit counter needs")
    void incrKeepsTtl() {
        redis.set("rate:rider-7", "0", SetOptions.px(60_000));
        redis.incr("rate:rider-7");
        redis.incr("rate:rider-7");
        assertEquals(60, redis.ttl("rate:rider-7"), "INCR changes the value but must keep the 60 s TTL");

        clock.advanceMillis(60_000);
        assertEquals(1, redis.incr("rate:rider-7"),
                "after the window expired, INCR should start again from 0, answering 1");
        assertEquals(-1, redis.ttl("rate:rider-7"), "the new counter was made by INCR, so it has no TTL: -1");
    }

    @Test
    @DisplayName("INCR on text that is not a whole number throws ERR and leaves the value alone")
    void incrNotInteger() {
        redis.set("rider:7:name", "Ada");
        RedisException error = assertThrows(RedisException.class, () -> redis.incr("rider:7:name"),
                "INCR on \"Ada\" should throw a RedisException");
        assertEquals("ERR value is not an integer or out of range", error.getMessage(),
                "the message should be Redis's own: ERR value is not an integer or out of range");
        assertEquals("Ada", redis.get("rider:7:name"), "a failed INCR must not change the value");

        redis.set("price", "12.5");
        assertThrows(RedisException.class, () -> redis.incr("price"), "INCR on \"12.5\" should throw: it is not a whole number");
    }

    @Test
    @DisplayName("DEL removes several keys and counts only the ones that existed")
    void del() {
        redis.set("a", "1");
        redis.set("b", "2");
        assertEquals(2, redis.del("a", "b", "c"), "DEL a b c should remove a and b and answer 2; c never existed");
        assertNull(redis.get("a"), "a should be gone after DEL");
        assertEquals(0, redis.del("a"), "deleting a again should answer 0");

        redis.set("lock:billing", "token-1", SetOptions.nxPx(30_000));
        redis.del("lock:billing");
        redis.set("lock:billing", "token-2");
        assertEquals(-1, redis.ttl("lock:billing"), "DEL must also forget the old TTL: the new key should have none");
    }

    @Test
    @DisplayName("SET NX only writes a missing key, answers null otherwise, and succeeds again once the old key expired")
    void nxAfterExpiry() {
        assertEquals("OK", redis.set("lock:billing", "token-1", SetOptions.nxPx(30_000)),
                "SET NX PX on a missing key should store it and answer \"OK\"");
        assertNull(redis.set("lock:billing", "server-b", SetOptions.nx()),
                "SET NX on a key that exists should answer null and change nothing");
        assertEquals("token-1", redis.get("lock:billing"), "the first value should still be there after the refused SET NX");
        clock.advanceMillis(29_999);
        assertNull(redis.set("lock:billing", "token-2", SetOptions.nxPx(30_000)),
                "29.999 s into a 30 s lease the key still exists, so SET NX should answer null");
        clock.advanceMillis(1);
        assertEquals("OK", redis.set("lock:billing", "token-2", SetOptions.nxPx(30_000)),
                "once the 30 s lease has run out, the expired key is invisible and SET NX should succeed");
        assertEquals("token-2", redis.get("lock:billing"), "the new value should be token-2");
    }

    @Test
    @DisplayName("Using a key with a command for another type throws WRONGTYPE")
    void wrongType() {
        redis.zadd("leaderboard:2026-W47", 3, "rider-7");
        redis.set("rider:7:name", "Ada");

        RedisException getOnZset = assertThrows(RedisException.class, () -> redis.get("leaderboard:2026-W47"),
                "GET on a sorted set should throw a RedisException");
        assertEquals(RedisException.WRONG_TYPE, getOnZset.getMessage(), "the message should be the WRONGTYPE error");
        assertThrows(RedisException.class, () -> redis.incr("leaderboard:2026-W47"), "INCR on a sorted set should throw WRONGTYPE");
        assertThrows(RedisException.class, () -> redis.zadd("rider:7:name", 1, "x"), "ZADD on a string should throw WRONGTYPE");
        assertThrows(RedisException.class, () -> redis.zscore("rider:7:name", "x"), "ZSCORE on a string should throw WRONGTYPE");

        assertEquals("OK", redis.set("leaderboard:2026-W47", "reset"), "SET may replace a key of any type");
        assertEquals("reset", redis.get("leaderboard:2026-W47"), "after SET, the key holds a string");
    }
}
