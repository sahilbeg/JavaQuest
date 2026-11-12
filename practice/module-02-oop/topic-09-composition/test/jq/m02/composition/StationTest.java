package jq.m02.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StationTest {

    @Test
    @DisplayName("A dock holds a bike and gives it back when released")
    void dockParksAndReleases() {
        Dock dock = new Dock();
        Bike bike = new Bike("B-1042");
        assertTrue(dock.isFree(), "a new Dock should be free: isFree() returns true when bike is null");
        dock.park(bike);
        assertFalse(dock.isFree(), "after park(bike) the dock should not be free");
        assertSame(bike, dock.release(), "release() should return the same bike that was parked");
        assertTrue(dock.isFree(), "after release() the dock should be empty again");
    }

    @Test
    @DisplayName("A new station has 12 free docks")
    void newStationHasTwelveFreeDocks() {
        Station station = new Station("Riverside");
        assertEquals(12, station.freeDocks(),
                "a new Station should have 12 free docks. Did the constructor put a new Dock() in every slot?");
    }

    @Test
    @DisplayName("Bikes go into the first free dock, numbered from 1")
    void dockUsesFirstFreeDock() {
        Station station = new Station("Harbor");
        assertEquals(1, station.dock(new Bike("B-1042")), "the first bike should go into dock 1 (index 0 plus 1)");
        assertEquals(2, station.dock(new Bike("B-2001")), "the second bike should go into dock 2");
        assertEquals(10, station.freeDocks(), "after parking 2 bikes, 10 docks should be free");
    }

    @Test
    @DisplayName("A full station returns -1 for the 13th bike")
    void fullStationReturnsMinusOne() {
        Station station = new Station("Uptown");
        for (int i = 1; i <= 12; i++) {
            station.dock(new Bike("B-" + (1000 + i)));
        }
        assertEquals(0, station.freeDocks(), "after 12 bikes, no dock should be free");
        assertEquals(-1, station.dock(new Bike("B-9999")), "dock(bike) should return -1 when all 12 docks are taken");
    }

    @Test
    @DisplayName("findDock and undock find a bike by its id")
    void undockFindsBikeById() {
        Station station = new Station("Old Mill");
        Bike first = new Bike("B-1042");
        Bike second = new Bike("B-2001");
        station.dock(first);
        station.dock(second);
        assertEquals(2, station.findDock("B-2001"), "findDock(\"B-2001\") should be 2, the dock it was parked in");
        assertEquals(-1, station.findDock("B-0000"), "findDock should return -1 for a bike that is not here");
        assertSame(second, station.undock("B-2001"), "undock(\"B-2001\") should return that same Bike object");
        assertEquals(-1, station.findDock("B-2001"), "after undock, the bike should no longer be in any dock");
        assertEquals(11, station.freeDocks(), "after undocking one of 2 bikes, 11 docks should be free");
    }

    @Test
    @DisplayName("undock returns null for a bike that is not parked")
    void undockUnknownReturnsNull() {
        Station station = new Station("Riverside");
        station.dock(new Bike("B-1042"));
        assertNull(station.undock("B-7777"), "undock should return null when no dock holds that bike id");
        assertEquals(11, station.freeDocks(), "undocking an unknown bike should not empty any dock");
    }
}
