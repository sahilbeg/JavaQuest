package jq.m10.launch;

import jakarta.validation.constraints.NotBlank;

/** Web adapter: the body of POST /rentals/{id}/end, for example {"stationId":"canal-street"}. */
// TODO 7 (stage 3): mark every field @NotBlank, so a missing or blank value is refused with 400.
public record EndRentalRequest(String stationId) {
}
