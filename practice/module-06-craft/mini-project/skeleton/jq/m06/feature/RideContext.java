package jq.m06.feature;

import java.time.DayOfWeek;

public record RideContext(String riderId, String type, int minutes, DayOfWeek day, int ridesThisMonth) {
}
