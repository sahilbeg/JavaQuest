package jq.m10.capstone;

import java.time.LocalDate;

/**
 * Core (domain event): a ride has ended. Billing and the reports listen for it.
 * serviceDate is the day the ride STARTED, in the city's own time zone.
 * eventId is "ride-ended:" plus the rental id, so a repeat of the same event has the same id.
 */
public record RideEnded(String eventId, String cityId, String rentalId, String riderId, String bikeId,
                        BikeType bikeType, long minutes, long fareCents, LocalDate serviceDate) {
}
