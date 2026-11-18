package jq.m10.launch;

import java.util.ArrayList;
import java.util.List;

/** Adapter (driven side): keeps every published event in a list. A stand-in for the outbox in tests and demos. */
public class RecordingEventPublisher implements EventPublisher {

    private final List<RideEnded> published = new ArrayList<>();

    @Override
    public synchronized void publish(RideEnded event) {
        published.add(event);
    }

    public synchronized List<RideEnded> published() {
        return List.copyOf(published);
    }
}
