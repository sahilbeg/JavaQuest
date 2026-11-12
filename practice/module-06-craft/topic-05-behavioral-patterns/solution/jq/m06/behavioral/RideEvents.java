package jq.m06.behavioral;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RideEvents {
    private final List<Consumer<String>> subscribers = new ArrayList<>();

    public Runnable subscribe(Consumer<String> subscriber) {
        subscribers.add(subscriber);
        return () -> subscribers.remove(subscriber);
    }

    public void rideEnded(String bikeId) {
        for (Consumer<String> subscriber : new ArrayList<>(subscribers)) {
            subscriber.accept(bikeId);
        }
    }
}
