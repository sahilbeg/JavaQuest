package jq.m10.launch;

import jakarta.validation.constraints.NotBlank;

/** Web adapter: the body of POST /rentals, for example {"riderId":"rider-7","bikeId":"BR-2003","stationId":"castle-hill"}. */
public record StartRentalRequest(@NotBlank String riderId, @NotBlank String bikeId, @NotBlank String stationId) {
}
