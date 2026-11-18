package jq.m12.redis;

/**
 * Proof that you hold a lock. Given to you, finished.
 *
 * @param name         the lock's name, such as "billing-2026-11-18" (the Redis key is "lock:" + name)
 * @param token        the random value stored in the lock key; only its owner knows it
 * @param fencingToken a number that grows by one every time anyone gets this lock
 */
public record Lease(String name, String token, long fencingToken) {
}
