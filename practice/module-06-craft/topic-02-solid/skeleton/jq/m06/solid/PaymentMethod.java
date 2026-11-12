package jq.m06.solid;

public interface PaymentMethod {
    String name();

    String pay(long cents);
}
