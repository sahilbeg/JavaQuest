package jq.m02.abstractclasses;

public class CargoBike extends Vehicle {

    public CargoBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        // TODO 7: a cargo bike costs 0.25 per minute
        return 0;
    }

    @Override
    public String kind() {
        // TODO 8: return "cargo"
        return "";
    }
}
