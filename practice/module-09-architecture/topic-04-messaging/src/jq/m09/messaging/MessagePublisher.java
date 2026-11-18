package jq.m09.messaging;

/** Sends a message to the broker. Throws a RuntimeException when the broker did not confirm it. */
@FunctionalInterface
public interface MessagePublisher {
    void publish(Message message);
}
