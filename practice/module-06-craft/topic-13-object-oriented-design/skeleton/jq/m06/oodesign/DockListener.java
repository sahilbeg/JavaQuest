package jq.m06.oodesign;

/** Hears when a dock becomes empty, so a truck driver or the live map can react. */
public interface DockListener {
    void dockFreed(int dockId);
}
