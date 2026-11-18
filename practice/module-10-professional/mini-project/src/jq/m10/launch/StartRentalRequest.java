package jq.m10.launch;

import jakarta.validation.constraints.NotBlank;

/** Web adapter: the body of POST /rentals, for example {"riderId":"rider-7","bikeId":"BR-2003","stationId":"castle-hill"}. */
// TODO 7 (stage 3): mark every field @NotBlank, so a missing or blank value is refused with 400.
public record StartRentalRequest(String riderId, String bikeId, String stationId) {
}
