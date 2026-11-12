package jq.m05.annotations;

public record KioskSettings(
        @NotBlank String stationName,
        @Range(min = 1, max = 12) int docks,
        @NotBlank String language) {
}
