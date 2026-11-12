package jq.m02.polymorphism;

public class FleetReport {

    public static double totalFare(Bike[] bikes, int minutes) {
        // TODO 1: add up b.fare(minutes) for every bike b in the array
        return 0.0;
    }

    public static int countEBikes(Bike[] bikes) {
        // TODO 2: count the bikes where b instanceof EBike is true
        return 0;
    }

    public static String lowBatteryIds(Bike[] bikes, int threshold) {
        // TODO 3: join the ids of e-bikes with battery below threshold, separated by ", "
        return "";
    }

    public static Bike cheapest(Bike[] bikes) {
        // TODO 4: return the bike with the lowest pricePerMinute(), or null if the array is empty
        return null;
    }

    public static void main(String[] args) {
        Bike[] fleet = {
            new Bike("B-1042"),
            new EBike("B-2001", 15),
            new CargoBike("B-3005"),
            new EBike("B-2002", 90)
        };
        System.out.println("Total for 10 minutes each: " + totalFare(fleet, 10));
        System.out.println("E-bikes: " + countEBikes(fleet));
        System.out.println("Low battery: " + lowBatteryIds(fleet, 20));
        Bike best = cheapest(fleet);
        if (best != null) {
            System.out.println("Cheapest: " + best.getId());
        }
    }
}
