package jq.m10.launch;

import jakarta.validation.constraints.NotBlank;

/** Web adapter: the body of POST /rentals/{id}/end, for example {"stationId":"canal-street"}. */
public record EndRentalRequest(@NotBlank String stationId) {
}
