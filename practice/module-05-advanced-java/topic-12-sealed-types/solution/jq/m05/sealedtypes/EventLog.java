package jq.m05.sealedtypes;

import java.util.List;

public class EventLog {

    public static String describe(DockEvent e) {
        if (e instanceof BikeDocked d) {
            return d.bikeId() + " docked at " + d.station();
        } else if (e instanceof BikeUndocked u) {
            return u.bikeId() + " left " + u.station();
        } else if (e instanceof Fault f) {
            return "Fault " + f.code() + " at " + f.station();
        }
        throw new IllegalStateException("Unknown dock event: " + e);
    }

    public static int freeDocksChange(List<DockEvent> events) {
        int change = 0;
        for (DockEvent e : events) {
            if (e instanceof BikeDocked) {
                change = change - 1;
            } else if (e instanceof BikeUndocked) {
                change = change + 1;
            }
        }
        return change;
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
