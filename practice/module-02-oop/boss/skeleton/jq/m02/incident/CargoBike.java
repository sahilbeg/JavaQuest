package jq.m02.incident;

public class CargoBike extends Bike {

    // TODO silver: Bike.pricePerMinute() still reads 0.15. Why does this line not change it?
    protected double price = 0.25;

    public CargoBike(String id) {
        super(id);
    }

    @Override
    public String type() {
        return "cargo";
    }
}
