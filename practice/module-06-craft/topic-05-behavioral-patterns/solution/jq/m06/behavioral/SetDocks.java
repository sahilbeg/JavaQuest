package jq.m06.behavioral;

public class SetDocks implements AdminCommand {
    private final Station station;
    private final int newDocks;
    private int oldDocks;

    public SetDocks(Station station, int newDocks) {
        this.station = station;
        this.newDocks = newDocks;
    }

    @Override
    public void execute() {
        oldDocks = station.docks();
        station.setDocks(newDocks);
    }

    @Override
    public void undo() {
        station.setDocks(oldDocks);
    }
}
