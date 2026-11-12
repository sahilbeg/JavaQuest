package jq.m05.patternmatching;

public class EventHandler {

    public static String describe(DockEvent e) {
        return switch (e) {
            case BikeDocked(var station, var bikeId) -> bikeId + " docked at " + station;
            case BikeUndocked(var station, var bikeId) -> bikeId + " left " + station;
            case Fault(var station, var code) -> "Fault " + code + " at " + station;
            case Rebalanced(var from, var to, var bikes) ->
                    "Truck moved " + bikes + " bikes from " + from + " to " + to;
        };
    }

    public static int priority(DockEvent e) {
        return switch (e) {
            case Fault f when f.code().startsWith("E") -> 1;
            case Fault f -> 2;
            case Rebalanced r when r.bikes() > 10 -> 3;
            default -> 4;
        };
    }

    public static String describeOrNone(Object o) {
        return switch (o) {
            case null -> "no event";
            case DockEvent e -> describe(e);
            default -> "unknown: " + o.getClass().getSimpleName();
        };
    }

    public static void main(String[] args) {
        DockEvent[] events = {
            new BikeDocked("Harbor", "B-1042"),
            new Fault("Harbor", "E7"),
            new Rebalanced("Riverside", "Harbor", 5)
        };
        for (DockEvent e : events) {
            System.out.println(priority(e) + " " + describe(e));
        }
        System.out.println(describeOrNone(null));
    }
}
