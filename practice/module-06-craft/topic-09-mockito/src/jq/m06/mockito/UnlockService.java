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
        // TODO 1: charge the rider UNLOCK_FEE_CENTS with gateway.charge and keep the payment id
        // TODO 2: if charge throws PaymentDeclinedException, return "declined" without touching the lock
        // TODO 3: open the lock with lock.open(bikeId); if it returns false, refund the payment id and return "lock failed"
        // TODO 4: send the receipt with the rider id and the payment id, then return "unlocked"
        return "";
    }
}
