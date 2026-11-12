package jq.m06.logging;

public class Checkout {

    // TODO 3: add a private static final Logger from LoggerFactory.getLogger(Checkout.class)

    public boolean pay(String riderId, String cardNumber, long cents) {
        // TODO 4: when cardNumber is null, log ERROR "payment failed for {}" with a new
        //         IllegalArgumentException as the last argument, and return false
        // TODO 5: never print the full card number; use PaymentLog.mask(cardNumber)
        if (cents > 50_000) {
            // TODO 6: log WARN "payment declined for {} card {}" instead of printing
            System.out.println("payment declined for " + riderId + " card " + cardNumber);
            return false;
        }
        // TODO 7: log INFO "payment ok for {} card {} amount {}" instead of printing
        System.out.println("payment ok for " + riderId + " card " + cardNumber + " amount " + cents);
        return true;
    }

    public static void main(String[] args) {
        Checkout checkout = new Checkout();
        checkout.pay("R-17", "4242 4242 4242 4242", 1250);
        checkout.pay("R-17", "4242 4242 4242 4242", 60_000);
        checkout.pay("R-17", null, 1250);
    }
}
