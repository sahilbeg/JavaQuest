package jq.m09.hexagonal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Tests the adapter alone, with a fake PayRiver client that records each request and answers as told.
class PayRiverPaymentAdapterTest {

    /** A stand-in for PayRiver's servers. */
    static class FakePayRiver implements PayRiverClient {
        final List<PayRiverChargeRequest> requests = new ArrayList<>();
        PayRiverResponse answer = new PayRiverResponse("ok", "pr_1");
        boolean unreachable = false;

        @Override
        public PayRiverResponse createCharge(PayRiverChargeRequest request) {
            requests.add(request);
            if (unreachable) {
                throw new PayRiverException("PayRiver timed out after 2000 ms");
            }
            return answer;
        }
    }

    final FakePayRiver payRiver = new FakePayRiver();
    final PayRiverPaymentAdapter adapter = new PayRiverPaymentAdapter(payRiver);

    @Test
    @DisplayName("The adapter sends PayRiver the rider, the rental, the amount as \"4.60\" and the currency USD")
    void translatesTheRequest() {
        PaymentResult result = adapter.charge("rider-7", "rental-17", 460);

        assertEquals(1, payRiver.requests.size(), "charge should call PayRiver's createCharge exactly once");
        PayRiverChargeRequest sent = payRiver.requests.get(0);
        assertEquals("rider-7", sent.customer(), "customer should be the rider id");
        assertEquals("4.60", sent.amount(), "460 cents should be sent as the text \"4.60\"");
        assertEquals("USD", sent.currency(), "currency should be \"USD\" (use the CURRENCY constant)");
        assertEquals("rental-17", sent.reference(), "reference should be the rental id");
        assertEquals(PaymentResult.PAID, result, "PayRiver's status \"ok\" should become PaymentResult.PAID");
    }

    @Test
    @DisplayName("Cents become a decimal string with two digits after the dot, for any amount")
    void formatsAmounts() {
        long[] cents = {100, 4605, 5, 0, 70, 123456};
        String[] expected = {"1.00", "46.05", "0.05", "0.00", "0.70", "1234.56"};
        for (int i = 0; i < cents.length; i++) {
            adapter.charge("rider-7", "rental-17", cents[i]);
            assertEquals(i + 1, payRiver.requests.size(), "charge should call PayRiver once per charge");
            assertEquals(expected[i], payRiver.requests.get(i).amount(),
                    cents[i] + " cents should be sent as \"" + expected[i] + "\". Watch out for 4.6 instead of 4.60.");
        }
    }

    @Test
    @DisplayName("The amount uses a dot even when the computer's language is German")
    void formatIgnoresTheMachineLanguage() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            adapter.charge("rider-7", "rental-17", 4605);
        } finally {
            Locale.setDefault(before);
        }
        assertEquals(1, payRiver.requests.size(), "charge should call PayRiver once");
        assertEquals("46.05", payRiver.requests.get(0).amount(),
                "PayRiver always wants a dot. String.format(\"%.2f\") writes 46,05 on a German computer. "
                        + "Use BigDecimal.valueOf(cents, 2).toPlainString().");
    }

    @Test
    @DisplayName("\"declined\" becomes DECLINED; any other status, a missing status or no answer becomes FAILED")
    void statusesAreTranslated() {
        payRiver.answer = new PayRiverResponse("declined", "pr_2");
        assertEquals(PaymentResult.DECLINED, adapter.charge("rider-7", "rental-17", 460),
                "PayRiver's status \"declined\" should become PaymentResult.DECLINED");
        String[] statuses = {"error", "pending", "insufficient_funds", null};
        for (String status : statuses) {
            payRiver.answer = new PayRiverResponse(status, "pr_3");
            assertEquals(PaymentResult.FAILED, adapter.charge("rider-7", "rental-17", 460),
                    "PayRiver's status " + (status == null ? "null" : "\"" + status + "\"") + " should become FAILED");
        }
        payRiver.answer = null;
        assertEquals(PaymentResult.FAILED, adapter.charge("rider-7", "rental-17", 460),
                "No answer at all (null) should become FAILED");
    }

    @Test
    @DisplayName("When PayRiver cannot be reached, the adapter returns FAILED instead of letting PayRiverException escape")
    void unreachableBecomesFailed() {
        payRiver.unreachable = true;

        PaymentResult result = null;
        try {
            result = adapter.charge("rider-7", "rental-17", 460);
        } catch (PayRiverException e) {
            throw new AssertionError("PayRiverException escaped from the adapter. Catch it and return "
                    + "PaymentResult.FAILED, so PayRiver's exception never reaches the core.", e);
        }
        assertNotNull(result, "charge should return a PaymentResult, not null");
        assertEquals(PaymentResult.FAILED, result, "An unreachable PayRiver should become PaymentResult.FAILED");
    }
}
