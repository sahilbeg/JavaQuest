package jq.m10.incident;

/**
 * A rider asks to hold one dock for a bike they are about to return.
 * Dock ids are the station id, a dash and the dock number: "harbor-3", "old-mill-12".
 */
public record ReservationRequest(String riderId, String dockId, BikeType bikeType) {

    /** The station part of the dock id: "old-mill-12" gives "old-mill". */
    public String stationId() {
        return dockId.substring(0, dockId.lastIndexOf('-'));
    }
}
