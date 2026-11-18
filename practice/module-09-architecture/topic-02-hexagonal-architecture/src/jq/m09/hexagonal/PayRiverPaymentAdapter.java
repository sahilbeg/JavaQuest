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
        // TODO 6: Build new PayRiverChargeRequest(riderId, toPayRiverAmount(amountCents), CURRENCY, rentalId),
        //         send it with client.createCharge, and return toResult(response).
        // TODO 7: If createCharge throws PayRiverException, return PaymentResult.FAILED.
        return null;
    }

    static String toPayRiverAmount(long cents) {
        // TODO 8: Turn cents into PayRiver's text: 460 -> "4.60", 5 -> "0.05". Hint: BigDecimal.valueOf(cents, 2).
        return "";
    }

    static PaymentResult toResult(PayRiverResponse response) {
        // TODO 9: "ok" -> PAID, "declined" -> DECLINED. Anything else, a null status or a null response -> FAILED.
        return null;
    }
}
