package jq.m04.greedyintervals;

/**
 * A time window in minutes after midnight, half-open: it includes start and stops just before end.
 * A booking from 600 to 660 uses minutes 600 to 659, so a booking starting at 660 does not overlap it.
 */
public record Interval(int start, int end) {

    public Interval {
        if (end <= start) {
            throw new IllegalArgumentException("A window must end after it starts, but got " + start + " to " + end);
        }
    }

    public boolean overlaps(Interval other) {
        return start < other.end && other.start < end;
    }
}
