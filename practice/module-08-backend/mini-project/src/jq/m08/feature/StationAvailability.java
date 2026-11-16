package jq.m08.feature;

/** What the API sends for one station. Jackson turns each field into a JSON property with the same name. */
public record StationAvailability(
        String id,
        String name,
        int docks,
        long bikesAvailable,
        long ebikesAvailable,
        long docksFree) {
}
