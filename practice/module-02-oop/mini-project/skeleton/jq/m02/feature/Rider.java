package jq.m02.feature;

public class Rider {

    private final String name;
    private Membership level;
    private final Ride[] rides = new Ride[50];
    private int count;

    public Rider(String name, Membership level) {
        this.name = name;
        this.level = level;
    }

    public void recordRide(Ride ride) {
        // TODO 4: store the ride, unless 50 rides are already stored
    }

    public int rideCount() {
        return count;
    }

    public void upgrade(Membership level) {
        // TODO 5: change the level
    }

    public double fareFor(Ride ride) {
        // TODO 6: free minutes first, then the price per minute; add 1.00 only for levels that pay the unlock fee
        return 0;
    }

    public double monthTotal() {
        // TODO 7: add up fareFor for every stored ride
        return 0;
    }

    public String statement() {
        // TODO 8: build the statement from the KB page
        return "";
    }

    public static void main(String[] args) {
        Rider ada = new Rider("Ada", Membership.PLUS);
        ada.recordRide(new Ride(BikeType.EBIKE, 40));
        ada.recordRide(new Ride(BikeType.CLASSIC, 20));
        ada.recordRide(new Ride(BikeType.CARGO, 90));
        System.out.println(ada.statement());
    }
}
