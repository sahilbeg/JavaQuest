package jq.m06.solid;

public class CardPayment implements PaymentMethod {
    @Override
    public String name() {
        return "card";
    }

    @Override
    public String pay(long cents) {
        return "card: charged " + cents + " cents";
    }
}
