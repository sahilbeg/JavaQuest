package jq.m02.classesobjects;

public class Bike {
    public String id;
    public String type;
    public int minutesRidden;

    public void addRide(int minutes) {
        minutesRidden = minutesRidden + minutes;
    }

    public String describe() {
        return id + " (" + type + "), " + minutesRidden + " min ridden";
    }

    public double fare(int minutes) {
        double rate = 0.0;
        if (type.equals("classic")) {
            rate = 0.15;
        } else if (type.equals("ebike")) {
            rate = 0.30;
        } else if (type.equals("cargo")) {
            rate = 0.25;
        }
        return 1.00 + minutes * rate;
    }
}
