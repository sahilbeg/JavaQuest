package jq.m03.comparatorsiterators;

public record Ride(String bikeId, String station, String type, int minutes) implements Comparable<Ride> {

    @Override
    public int compareTo(Ride other) {
        // TODO 1: compare this.minutes with other.minutes using Integer.compare
        return 0;
    }
}
