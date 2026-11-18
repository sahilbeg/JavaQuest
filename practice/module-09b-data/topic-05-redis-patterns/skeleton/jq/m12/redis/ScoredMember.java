package jq.m12.redis;

/** One line of a ZREVRANGE ... WITHSCORES answer: a member and its score. Given to you, finished. */
public record ScoredMember(String member, double score) {
}
