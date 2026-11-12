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
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    public static String run(Station s, String bikeId) {
        try (KioskSession session = new KioskSession(s)) {
            return "Docked at " + session.dock(bikeId);
        } catch (DockFullException e) {
            return e.getMessage();
        }
    }

    public static void main(String[] args) {
        Station harbor = new Station("Harbor", 2);
        System.out.println(run(harbor, "B-1042"));
        System.out.println(run(harbor, "B-2001"));
        System.out.println(run(harbor, "B-3300"));
    }
}
