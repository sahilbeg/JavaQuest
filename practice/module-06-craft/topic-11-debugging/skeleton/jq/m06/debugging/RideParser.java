package jq.m06.debugging;

public class RideParser {

    // TODO 1: crash report 1 says a line ending in an empty note crashes here. Fix it, and reject lines without 4 fields.
    public ParsedRide parse(String csv) {
        String[] parts = csv.split(",");
        String bikeId = parts[0];
        String station = parts[1];
        int minutes = Integer.parseInt(parts[2]);
        String note = parts[3];
        return new ParsedRide(bikeId, station, minutes, note);
    }
}
