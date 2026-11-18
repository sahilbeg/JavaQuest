package jq.m09.incident;

/**
 * One request from the rider app's live map. Ready to use: do not change.
 *
 * @param riderId   who is looking, for example "rider-7"
 * @param requestId a new id for every request, for example "req-90211"
 * @param stationId which station's numbers the map needs, for example "riverside"
 */
public record MapRequest(String riderId, String requestId, String stationId) {
}
