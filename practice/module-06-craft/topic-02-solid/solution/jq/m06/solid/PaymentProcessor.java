package jq.m06.solid;

import java.util.List;

public class PaymentProcessor {

    private final List<PaymentMethod> methods;

    public PaymentProcessor(List<PaymentMethod> methods) {
        this.methods = List.copyOf(methods);
    }

    public String pay(String method, long cents) {
        for (PaymentMethod candidate : methods) {
            if (candidate.name().equals(method)) {
                return candidate.pay(cents);
            }
        }
        throw new IllegalArgumentException("unknown payment method: " + method);
    }

    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor(
                List.of(new CardPayment(), new WalletPayment(), new TransitPassPayment()));
        System.out.println(processor.pay("card", 250));
        System.out.println(processor.pay("transit", 250));
    }
}
