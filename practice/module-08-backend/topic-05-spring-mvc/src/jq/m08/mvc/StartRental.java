package jq.m08.mvc;

/** The JSON body the app sends to start a rental, for example {"riderId":"r-17","bikeId":"B-1042","station":"harbor"}. */
public record StartRental(String riderId, String bikeId, String station) {
}
