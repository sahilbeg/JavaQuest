package jq.m06.debugging;

public class RideParser {

    public ParsedRide parse(String csv) {
        String[] parts = csv.split(",", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("expected 4 fields but got " + parts.length + ": " + csv);
        }
        String bikeId = parts[0];
        String station = parts[1];
        int minutes = Integer.parseInt(parts[2]);
        String note = parts[3];
        return new ParsedRide(bikeId, station, minutes, note);
    }
}
