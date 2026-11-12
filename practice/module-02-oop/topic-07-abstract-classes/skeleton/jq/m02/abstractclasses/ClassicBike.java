package jq.m02.abstractclasses;

public class ClassicBike extends Vehicle {

    public ClassicBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        // TODO 3: a classic bike costs 0.15 per minute
        return 0;
    }

    @Override
    public String kind() {
        // TODO 4: return "classic"
        return "";
    }
}
