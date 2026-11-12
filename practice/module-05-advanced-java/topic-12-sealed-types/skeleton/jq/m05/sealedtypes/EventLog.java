package jq.m05.sealedtypes;

import java.util.List;

public class EventLog {

    public static String describe(DockEvent e) {
        // TODO 2: use instanceof with a pattern variable for each of the three event types
        //         "B-1042 docked at Harbor", "B-1042 left Harbor", "Fault E7 at Harbor"
        return "";
    }

    public static int freeDocksChange(List<DockEvent> events) {
        // TODO 3: add -1 for each BikeDocked, +1 for each BikeUndocked and 0 for each Fault
        return 0;
    }

    public static void main(String[] args) {
        List<DockEvent> events = List.of(
                new BikeDocked("Harbor", "B-1042"),
                new Fault("Harbor", "E7"),
                new BikeUndocked("Harbor", "B-1042"),
                new BikeUndocked("Harbor", "B-2001"));
        for (DockEvent e : events) {
            System.out.println(describe(e));
        }
        System.out.println("Free docks change: " + freeDocksChange(events));
    }
}
