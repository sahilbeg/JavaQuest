package jq.m06.solid;

public class TransitPassPayment implements PaymentMethod {
    @Override
    public String name() {
        return "transit";
    }

    @Override
    public String pay(long cents) {
        return "transit: charged " + cents + " cents";
    }
}
