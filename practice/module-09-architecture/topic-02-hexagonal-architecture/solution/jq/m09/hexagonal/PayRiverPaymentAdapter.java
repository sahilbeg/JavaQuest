package jq.m09.hexagonal;

import java.math.BigDecimal;

/**
 * Adapter (driven side) and anti-corruption layer: it speaks Spoke's PaymentPort on one side
 * and PayRiver's formats on the other. PayRiver's types never get past this class.
 */
public class PayRiverPaymentAdapter implements PaymentPort {

    static final String CURRENCY = "USD";

    private final PayRiverClient client;

    public PayRiverPaymentAdapter(PayRiverClient client) {
        this.client = client;
    }

    @Override
    public PaymentResult charge(String riderId, String rentalId, long amountCents) {
        PayRiverChargeRequest request =
                new PayRiverChargeRequest(riderId, toPayRiverAmount(amountCents), CURRENCY, rentalId);
        try {
            return toResult(client.createCharge(request));
        } catch (PayRiverException e) {
            return PaymentResult.FAILED;
        }
    }

    /** 460 cents becomes "4.60". BigDecimal with scale 2 never uses a comma, whatever the machine's language. */
    static String toPayRiverAmount(long cents) {
        return BigDecimal.valueOf(cents, 2).toPlainString();
    }

    static PaymentResult toResult(PayRiverResponse response) {
        if (response == null || response.status() == null) {
            return PaymentResult.FAILED;
        }
        return switch (response.status()) {
            case "ok" -> PaymentResult.PAID;
            case "declined" -> PaymentResult.DECLINED;
            default -> PaymentResult.FAILED;
        };
    }
}
