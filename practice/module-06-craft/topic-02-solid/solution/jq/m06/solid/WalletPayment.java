package jq.m06.solid;

public class WalletPayment implements PaymentMethod {
    @Override
    public String name() {
        return "wallet";
    }

    @Override
    public String pay(long cents) {
        return "wallet: charged " + cents + " cents";
    }
}
