package jq.m06.creational;

public class ClassicBike implements Bike {

    @Override
    public String type() {
        return "classic";
    }

    @Override
    public int centsPerMinute() {
        return PriceTable.INSTANCE.centsPerMinute("classic");
    }
}
