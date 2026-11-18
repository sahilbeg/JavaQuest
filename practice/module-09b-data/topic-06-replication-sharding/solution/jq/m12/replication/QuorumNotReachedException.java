package jq.m12.replication;

/** Thrown when fewer replicas answered than the read quorum needs. Given to you, finished. */
public class QuorumNotReachedException extends RuntimeException {

    public QuorumNotReachedException(String message) {
        super(message);
    }
}
