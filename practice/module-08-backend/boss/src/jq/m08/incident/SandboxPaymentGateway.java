package jq.m08.incident;

import org.springframework.stereotype.Component;

/** The bank's test system: it accepts every rider except r-broke. */
@Component
public class SandboxPaymentGateway implements PaymentGateway {

    @Override
    public void hold(String riderId, long cents) throws PaymentDeclinedException {
        if (riderId.equals("r-broke")) {
            throw new PaymentDeclinedException(riderId);
        }
    }
}
