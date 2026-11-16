package jq.m06.behavioral;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RideEvents {
    // TODO 4: keep a list of subscribers

    public Runnable subscribe(Consumer<String> subscriber) {
        // TODO 5: add the subscriber and return a Runnable that removes it again
        return () -> { };
    }

    public void rideEnded(String bikeId) {
        // TODO 6: call accept(bikeId) on every current subscriber, in the order they subscribed
    }
}
