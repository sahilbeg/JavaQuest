package jq.m10.capstone;

/**
 * Core (read model): the numbers the station map shows.
 * bikesAvailable counts docked bikes a rider can take here (types the city offers); ebikesAvailable is the EBIKE part of it.
 * docksFree is docks minus every bike docked here, whatever its type.
 */
public record StationAvailability(String id, String name, int docks, int bikesAvailable, int ebikesAvailable,
                                  int docksFree) {
}
