package jq.m06.creational;

public class CargoBike implements Bike {

    @Override
    public String type() {
        return "cargo";
    }

    @Override
    public int centsPerMinute() {
        return PriceTable.INSTANCE.centsPerMinute("cargo");
    }
}
