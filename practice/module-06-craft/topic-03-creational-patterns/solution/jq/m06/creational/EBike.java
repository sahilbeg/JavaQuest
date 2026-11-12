package jq.m06.creational;

public class EBike implements Bike {

    @Override
    public String type() {
        return "ebike";
    }

    @Override
    public int centsPerMinute() {
        return PriceTable.INSTANCE.centsPerMinute("ebike");
    }
}
