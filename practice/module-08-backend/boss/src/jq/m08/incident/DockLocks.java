package jq.m08.incident;

import org.springframework.stereotype.Component;

/** Sends the unlock signal to the dock. In this project every signal arrives. */
@Component
public class DockLocks implements BikeLock {

    @Override
    public void open(String bikeId) {
    }
}
