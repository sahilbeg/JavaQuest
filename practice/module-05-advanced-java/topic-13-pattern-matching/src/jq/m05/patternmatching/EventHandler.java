package jq.m05.patternmatching;

public class EventHandler {

    public static String describe(DockEvent e) {
        // TODO 1: replace this switch with one case per record pattern and no default
        //         "B-1042 docked at Harbor", "B-1042 left Harbor", "Fault E7 at Harbor",
        //         "Truck moved 5 bikes from Riverside to Harbor"
        return switch (e) {
            default -> "";
        };
    }

    public static int priority(DockEvent e) {
        // TODO 2: use guards with when: Fault with a code starting "E" is 1, any other Fault is 2,
        //         Rebalanced with more than 10 bikes is 3, everything else is 4
        return switch (e) {
            default -> 0;
        };
    }

    public static String describeOrNone(Object o) {
        // TODO 3: case null gives "no event", a DockEvent goes to describe(...),
        //         anything else gives "unknown: " plus the class simple name
        return switch (o) {
            case null -> "";
            default -> "";
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
