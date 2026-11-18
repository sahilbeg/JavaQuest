package jq.m09.hexagonal;

/** Core (domain): what ending a ride produced. The use case returns it, and the notifier gets it. */
public record RideReceipt(String rentalId, String riderId, int minutes, long fareCents, PaymentResult payment) {
}
