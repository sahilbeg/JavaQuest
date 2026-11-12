package jq.m06.creational;

// TODO 7: turn this class into an enum with exactly one constant, INSTANCE.
// Right now anyone can call new PriceTable(), and the app makes a fresh one per request.
public class PriceTable {

    public static final PriceTable INSTANCE = new PriceTable();

    public PriceTable() {
    }

    public int centsPerMinute(String type) {
        if (type.equals("ebike")) {
            return 30;
        }
        if (type.equals("cargo")) {
            return 25;
        }
        return 15;
    }
}
