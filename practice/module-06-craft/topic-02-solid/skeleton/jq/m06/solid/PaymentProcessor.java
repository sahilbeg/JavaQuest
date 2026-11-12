package jq.m06.solid;

import java.util.List;

public class PaymentProcessor {

    public PaymentProcessor(List<PaymentMethod> methods) {
        // TODO 4: keep the methods, so pay can look them up by name.
    }

    public String pay(String method, long cents) {
        // TODO 5: replace this switch. Find the PaymentMethod whose name() matches and call its pay.
        //         Throw IllegalArgumentException("unknown payment method: " + method) when none matches.
        switch (method) {
            case "card":
                return "card: charged " + cents + " cents";
            case "wallet":
                return "wallet: charged " + cents + " cents";
            default:
                throw new IllegalStateException("not supported yet");
        }
    }

    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor(List.of());
        System.out.println(processor.pay("card", 250));
    }
}
