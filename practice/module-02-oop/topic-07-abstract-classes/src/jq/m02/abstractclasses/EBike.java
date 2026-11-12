package jq.m02.abstractclasses;

public class EBike extends Vehicle {

    public EBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        // TODO 5: an e-bike costs 0.30 per minute
        return 0;
    }

    @Override
    public String kind() {
        // TODO 6: return "ebike"
        return "";
    }
}
