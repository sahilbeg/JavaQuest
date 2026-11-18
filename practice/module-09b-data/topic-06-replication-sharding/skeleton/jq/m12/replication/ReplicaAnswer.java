package jq.m12.replication;

/**
 * One replica's answer to a quorum read. Given to you, finished.
 *
 * @param replica the replica's name
 * @param value   the value it holds, or null when it holds no copy
 * @param version the value's version number; 0 when it holds no copy
 */
public record ReplicaAnswer(String replica, String value, long version) {
}
