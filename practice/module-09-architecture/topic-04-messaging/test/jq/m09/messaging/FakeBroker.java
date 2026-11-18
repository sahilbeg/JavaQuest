package jq.m09.messaging;

import java.util.ArrayList;
import java.util.List;

/** A pretend broker. It keeps every message it received, and it can fail on purpose. */
final class FakeBroker implements MessagePublisher {

    final List<Message> received = new ArrayList<>();
    int calls;
    /** The publish call (1, 2, 3...) that fails before the broker stores the message. 0 means never. */
    int failOnCall;
    /** The publish call that stores the message, but whose "got it" answer is lost, so it still throws. */
    int ackLostOnCall;

    @Override
    public void publish(Message message) {
        calls++;
        if (calls == failOnCall) {
            throw new IllegalStateException("broker unavailable");
        }
        received.add(message);
        if (calls == ackLostOnCall) {
            throw new IllegalStateException("timed out waiting for the broker to confirm");
        }
    }

    /** The event ids received so far, in order, joined with ", ". */
    String eventIds() {
        return String.join(", ", received.stream().map(Message::eventId).toList());
    }
}
