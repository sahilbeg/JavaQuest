package jq.m03.comparatorsiterators;

public record Ride(String bikeId, String station, String type, int minutes) implements Comparable<Ride> {

    @Override
    public int compareTo(Ride other) {
        return Integer.compare(this.minutes, other.minutes);
    }
}
