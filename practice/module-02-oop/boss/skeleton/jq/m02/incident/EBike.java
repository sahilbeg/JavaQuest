package jq.m02.incident;

public class EBike extends Bike {

    public EBike(String id) {
        super(id);
    }

    @Override
    public String type() {
        return "ebike";
    }

    // TODO bronze: this method never runs when the invoice calls fare(). Why not?
    public double pricePerMinute(int minutes) {
        return 0.30;
    }
}
