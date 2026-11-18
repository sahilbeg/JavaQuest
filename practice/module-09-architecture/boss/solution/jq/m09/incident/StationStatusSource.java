package jq.m09.incident;

/**
 * The slow station-status service. Each call costs real work: it counts bikes and docks.
 * Ready to use: do not change. The checks use a fake that counts calls.
 */
public interface StationStatusSource {

    /**
     * Loads the live numbers for one station.
     *
     * @throws RuntimeException when the service is down or overloaded
     */
    StationStatus load(String stationId);
}
