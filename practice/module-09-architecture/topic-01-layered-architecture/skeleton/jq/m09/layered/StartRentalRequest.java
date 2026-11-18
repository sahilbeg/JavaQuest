package jq.m09.layered;

import jakarta.validation.constraints.NotBlank;

/**
 * Web layer: the JSON body of POST /rentals, for example
 * {"riderId":"rider-7","bikeId":"B-1042","stationId":"harbor","paymentToken":"tok_visa_4242"}.
 * The annotations check its shape at the edge, before any rule runs.
 */
public record StartRentalRequest(
        @NotBlank String riderId,
        @NotBlank String bikeId,
        @NotBlank String stationId,
        @NotBlank String paymentToken) {
}
