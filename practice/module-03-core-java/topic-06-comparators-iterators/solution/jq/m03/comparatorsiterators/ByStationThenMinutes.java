package jq.m03.comparatorsiterators;

import java.util.Comparator;

public class ByStationThenMinutes implements Comparator<Ride> {

    @Override
    public int compare(Ride a, Ride b) {
        int byStation = a.station().compareTo(b.station());
        if (byStation != 0) {
            return byStation;
        }
        return Integer.compare(a.minutes(), b.minutes());
    }
}
