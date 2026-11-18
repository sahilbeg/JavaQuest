package jq.m09.incident;

/**
 * What the live map shows for one station. Ready to use: do not change.
 *
 * @param stationId      for example "riverside"
 * @param bikesAvailable bikes a rider can take right now
 * @param docksFree      empty docks a rider can return a bike to
 * @param stale          true when this is an old copy, served because the source is down
 */
public record StationStatus(String stationId, int bikesAvailable, int docksFree, boolean stale) {

    /** A fresh status, straight from the source. */
    public static StationStatus live(String stationId, int bikesAvailable, int docksFree) {
        return new StationStatus(stationId, bikesAvailable, docksFree, false);
    }

    /** The same numbers, marked as an old copy. */
    public StationStatus asStale() {
        return new StationStatus(stationId, bikesAvailable, docksFree, true);
    }
}
