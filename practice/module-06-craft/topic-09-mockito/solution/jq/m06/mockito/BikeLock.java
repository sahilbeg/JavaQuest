package jq.m06.mockito;

/** The physical lock on a dock. */
public interface BikeLock {

    /** Tries to open the lock that holds this bike. Returns true when it opened. */
    boolean open(String bikeId);
}
