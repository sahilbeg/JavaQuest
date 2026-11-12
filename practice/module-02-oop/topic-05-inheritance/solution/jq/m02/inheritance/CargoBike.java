package jq.m02.inheritance;

public class CargoBike extends Bike {

    public CargoBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.25;
    }

    @Override
    public String describe() {
        return super.describe() + " [cargo]";
    }
}
