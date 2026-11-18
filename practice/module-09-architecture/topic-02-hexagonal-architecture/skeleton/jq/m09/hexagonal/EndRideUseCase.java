package jq.m09.hexagonal;

/**
 * Core, driving port: what the outside world can ask the core to do.
 * A web controller, a kiosk, a command-line tool or a test calls it.
 */
public interface EndRideUseCase {

    RideReceipt endRide(String rentalId);
}
