package jq.m06.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Checkout {

    private static final Logger log = LoggerFactory.getLogger(Checkout.class);

    public boolean pay(String riderId, String cardNumber, long cents) {
        if (cardNumber == null) {
            log.error("payment failed for {}", riderId, new IllegalArgumentException("card number is missing"));
            return false;
        }
        String masked = PaymentLog.mask(cardNumber);
        if (cents > 50_000) {
            log.warn("payment declined for {} card {}", riderId, masked);
            return false;
        }
        log.info("payment ok for {} card {} amount {}", riderId, masked, cents);
        return true;
    }

    public static void main(String[] args) {
        Checkout checkout = new Checkout();
        checkout.pay("R-17", "4242 4242 4242 4242", 1250);
        checkout.pay("R-17", "4242 4242 4242 4242", 60_000);
        checkout.pay("R-17", null, 1250);
    }
}
