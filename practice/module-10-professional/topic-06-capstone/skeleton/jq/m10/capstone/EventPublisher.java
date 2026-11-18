package jq.m10.capstone;

/** Core, driven port: tells the rest of Spoke that something happened. An outbox or a message broker implements it. */
public interface EventPublisher {

    void publish(RideEnded event);
}
