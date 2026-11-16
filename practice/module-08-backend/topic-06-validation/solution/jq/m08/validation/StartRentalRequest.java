package jq.m08.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** The JSON body for POST /rentals, for example {"riderId":"r-17","bikeId":"B-1042","station":"harbor"}. */
public record StartRentalRequest(
        @NotBlank String riderId,
        @NotNull @Pattern(regexp = "B-\\d{4}", message = "must look like B-1042") String bikeId,
        @NotBlank String station) {
}
