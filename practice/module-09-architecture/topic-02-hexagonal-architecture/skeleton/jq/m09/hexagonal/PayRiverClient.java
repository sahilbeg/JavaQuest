package jq.m09.hexagonal;

/**
 * PayRiver's client library, as PayRiver ships it. Do not change it: it is someone else's code.
 * createCharge throws PayRiverException when PayRiver cannot be reached.
 */
public interface PayRiverClient {

    PayRiverResponse createCharge(PayRiverChargeRequest request);
}
