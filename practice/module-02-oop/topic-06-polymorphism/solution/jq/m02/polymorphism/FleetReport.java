package jq.m02.polymorphism;

public class FleetReport {

    public static double totalFare(Bike[] bikes, int minutes) {
        double total = 0.0;
        for (Bike b : bikes) {
            total += b.fare(minutes);
        }
        return total;
    }

    public static int countEBikes(Bike[] bikes) {
        int count = 0;
        for (Bike b : bikes) {
            if (b instanceof EBike) {
                count++;
            }
        }
        return count;
    }

    public static String lowBatteryIds(Bike[] bikes, int threshold) {
        StringBuilder sb = new StringBuilder();
        for (Bike b : bikes) {
            if (b instanceof EBike e && e.getBattery() < threshold) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(e.getId());
            }
        }
        return sb.toString();
    }

    public static Bike cheapest(Bike[] bikes) {
        if (bikes.length == 0) {
            return null;
        }
        Bike best = bikes[0];
        for (Bike b : bikes) {
            if (b.pricePerMinute() < best.pricePerMinute()) {
                best = b;
            }
        }
        return best;
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
        System.out.println("Cheapest: " + cheapest(fleet).getId());
    }
}
