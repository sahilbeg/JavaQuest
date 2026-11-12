package jq.m06.mockito;

/** Unlocks a bike for a rider: charge the unlock fee, open the lock, send a receipt. */
public class UnlockService {

    static final long UNLOCK_FEE_CENTS = 100;

    private final PaymentGateway gateway;
    private final BikeLock lock;
    private final Receipts receipts;

    public UnlockService(PaymentGateway gateway, BikeLock lock, Receipts receipts) {
        this.gateway = gateway;
        this.lock = lock;
        this.receipts = receipts;
    }

    /** Returns "unlocked", "declined" or "lock failed". */
    public String unlock(String riderId, String bikeId) {
        String paymentId;
        try {
            paymentId = gateway.charge(riderId, UNLOCK_FEE_CENTS);
        } catch (PaymentDeclinedException e) {
            return "declined";
        }
        if (!lock.open(bikeId)) {
            gateway.refund(paymentId);
            return "lock failed";
        }
        receipts.send(riderId, paymentId);
        return "unlocked";
    }
}
