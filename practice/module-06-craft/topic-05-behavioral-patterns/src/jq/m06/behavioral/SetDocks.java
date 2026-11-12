package jq.m06.behavioral;

public class SetDocks implements AdminCommand {
    private final Station station;
    private final int newDocks;

    public SetDocks(Station station, int newDocks) {
        this.station = station;
        this.newDocks = newDocks;
    }

    @Override
    public void execute() {
        // TODO 7: remember the station's current docks, then set them to newDocks
    }

    @Override
    public void undo() {
        // TODO 8: put the remembered docks value back
    }
}
