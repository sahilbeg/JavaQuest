package jq.m09.hexagonal;

/** Core, driven port: tells the rider their ride ended. A push, SMS or email adapter plugs in here. */
public interface RiderNotifier {

    void rideEnded(RideReceipt receipt);
}
