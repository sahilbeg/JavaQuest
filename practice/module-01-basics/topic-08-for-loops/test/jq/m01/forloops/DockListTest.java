package jq.m01.forloops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DockListTest {

    @Test
    @DisplayName("dockLine lists every dock with single spaces and no space at the end")
    void dockLine() {
        assertEquals("1 2 3 4 5", DockList.dockLine(5),
                "dockLine(5) should be \"1 2 3 4 5\". Check for a space at the start or the end");
        assertEquals("1 2 3 4 5 6 7 8 9 10 11 12", DockList.dockLine(12),
                "dockLine(12) should list all 12 kiosk docks. Use i <= docks so the last dock is included");
        assertEquals("", DockList.dockLine(0),
                "dockLine(0) should be an empty String: there are no docks to list");
    }

    @Test
    @DisplayName("evenDocks lists only the even dock numbers")
    void evenDocks() {
        assertEquals("2 4 6 8 10 12", DockList.evenDocks(12),
                "evenDocks(12) should be \"2 4 6 8 10 12\". Start at 2 and step with i += 2");
        assertEquals("2 4", DockList.evenDocks(5),
                "evenDocks(5) should be \"2 4\": 6 is past the last dock");
    }

    @Test
    @DisplayName("totalClassicFare adds one classic fare per ride")
    void totalClassicFare() {
        assertEquals(7.5, DockList.totalClassicFare(3, 10), 0.001,
                "totalClassicFare(3, 10) should be 7.5: three rides of 1.00 plus 10 minutes at 0.15, which is 2.50 each");
        assertEquals(1.0, DockList.totalClassicFare(1, 0), 0.001,
                "totalClassicFare(1, 0) should be 1.0: just the unlock fee");
        assertEquals(0.0, DockList.totalClassicFare(0, 20), 0.001,
                "totalClassicFare(0, 20) should be 0.0: no rides means no fare");
    }

    @Test
    @DisplayName("firstFreeDock finds the first dock after the taken ones")
    void firstFreeDock() {
        assertEquals(8, DockList.firstFreeDock(12, 7),
                "firstFreeDock(12, 7) should be 8: docks 1 to 7 are taken");
        assertEquals(1, DockList.firstFreeDock(12, 0),
                "firstFreeDock(12, 0) should be 1: no dock is taken");
    }

    @Test
    @DisplayName("firstFreeDock gives -1 when every dock is taken")
    void noFreeDock() {
        assertEquals(-1, DockList.firstFreeDock(12, 12),
                "firstFreeDock(12, 12) should be -1: all 12 docks are taken");
    }
}
