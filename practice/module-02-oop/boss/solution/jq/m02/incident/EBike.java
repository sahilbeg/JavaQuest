package jq.m02.incident;

public class EBike extends Bike {

    public EBike(String id) {
        super(id);
    }

    @Override
    public String type() {
        return "ebike";
    }

    @Override
    public double pricePerMinute() {
        return 0.30;
    }
}
