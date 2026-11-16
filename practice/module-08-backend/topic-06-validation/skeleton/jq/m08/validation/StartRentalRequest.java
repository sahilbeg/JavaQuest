package jq.m08.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** The JSON body for POST /rentals, for example {"riderId":"r-17","bikeId":"B-1042","station":"harbor"}. */
public record StartRentalRequest(
        // TODO 1: add @NotBlank to riderId and station, and @NotNull plus @Pattern(regexp = "B-\\d{4}", message = "must look like B-1042") to bikeId
        String riderId,
        String bikeId,
        String station) {
}
