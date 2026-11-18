package jq.m12.replication;

import java.util.List;

/**
 * What a quorum read decides. Given to you, finished.
 *
 * @param value      the newest value seen, or null if no replica holds one
 * @param version    its version
 * @param needRepair the replicas that answered with an older version, in the order they answered
 */
public record ReadResult(String value, long version, List<String> needRepair) {
}
