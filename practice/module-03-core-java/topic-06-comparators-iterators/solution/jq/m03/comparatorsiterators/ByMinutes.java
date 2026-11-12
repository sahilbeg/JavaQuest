package jq.m03.comparatorsiterators;

import java.util.Comparator;

public class ByMinutes implements Comparator<Ride> {

    @Override
    public int compare(Ride a, Ride b) {
        return Integer.compare(a.minutes(), b.minutes());
    }
}
