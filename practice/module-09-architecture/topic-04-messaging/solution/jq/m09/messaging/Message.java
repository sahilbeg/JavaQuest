package jq.m09.messaging;

/**
 * One message as the broker sees it.
 *
 * @param eventId the event's unique id, such as "evt-1001"; consumers use it to spot repeats
 * @param topic   where it goes, such as "ride-events"
 * @param key     the partition key, such as "rider-7"; messages with one key stay in order
 * @param payload the event itself, as JSON text
 */
public record Message(String eventId, String topic, String key, String payload) {}
