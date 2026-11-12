package jq.m03.customexceptions;

public class KioskSession implements AutoCloseable {

    private final Station station;
    private boolean closed;

    public KioskSession(Station station) {
        this.station = station;
    }

    public int dock(String bikeId) throws DockFullException {
        return station.dock(bikeId);
    }

    @Override
    public void close() {
        // TODO 7: remember that the session is closed
    }

    public boolean isClosed() {
        return closed;
    }

    public static String run(Station s, String bikeId) {
        // TODO 8: open a KioskSession in try-with-resources and return "Docked at " + the dock number
        // TODO 9: catch DockFullException and return its message
        return "";
    }

    public static void main(String[] args) {
        Station harbor = new Station("Harbor", 2);
        System.out.println(run(harbor, "B-1042"));
        System.out.println(run(harbor, "B-2001"));
        System.out.println(run(harbor, "B-3300"));
    }
}
