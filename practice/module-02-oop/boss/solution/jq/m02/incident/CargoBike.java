package jq.m02.incident;

public class CargoBike extends Bike {

    public CargoBike(String id) {
        super(id);
        price = 0.25;
    }

    @Override
    public String type() {
        return "cargo";
    }
}
