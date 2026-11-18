package jq.m09.incident;

/**
 * Thrown when the cache has nothing it may show for a station:
 * the source failed, and there is no old copy, or the old copy is too old.
 * Ready to use: do not change.
 */
public class StatusUnavailableException extends RuntimeException {

    private final String stationId;

    public StatusUnavailableException(String stationId, Throwable cause) {
        super("No status available for station " + stationId, cause);
        this.stationId = stationId;
    }

    public String stationId() {
        return stationId;
    }
}
