package jq.m05.patternmatching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventHandlerTest {

    @Test
    @DisplayName("describe() writes docked, undocked and fault messages")
    void describeDockEvents() {
        assertEquals("B-1042 docked at Harbor", EventHandler.describe(new BikeDocked("Harbor", "B-1042")),
                "A BikeDocked event should read: <bikeId> docked at <station>");
        assertEquals("B-1042 left Harbor", EventHandler.describe(new BikeUndocked("Harbor", "B-1042")),
                "A BikeUndocked event should read: <bikeId> left <station>");
        assertEquals("Fault E7 at Old Mill", EventHandler.describe(new Fault("Old Mill", "E7")),
                "A Fault event should read: Fault <code> at <station>");
    }

    @Test
    @DisplayName("describe() writes a truck message for a Rebalanced event")
    void describeRebalanced() {
        assertEquals("Truck moved 5 bikes from Riverside to Harbor",
                EventHandler.describe(new Rebalanced("Riverside", "Harbor", 5)),
                "A Rebalanced event should read: Truck moved <bikes> bikes from <from> to <to>");
    }

    @Test
    @DisplayName("priority() ranks faults by their code")
    void priorityOfFaults() {
        assertEquals(1, EventHandler.priority(new Fault("Harbor", "E7")),
                "A Fault whose code starts with E is urgent, so its priority should be 1");
        assertEquals(2, EventHandler.priority(new Fault("Harbor", "W2")),
                "A Fault whose code does not start with E should have priority 2");
    }

    @Test
    @DisplayName("priority() ranks big truck moves above ordinary events")
    void priorityOfOthers() {
        assertEquals(3, EventHandler.priority(new Rebalanced("Uptown", "Depot", 11)),
                "A Rebalanced event with more than 10 bikes should have priority 3");
        assertEquals(4, EventHandler.priority(new Rebalanced("Uptown", "Depot", 10)),
                "A Rebalanced event with exactly 10 bikes is not more than 10, so its priority should be 4");
        assertEquals(4, EventHandler.priority(new BikeDocked("Harbor", "B-1042")),
                "A BikeDocked event should have priority 4");
    }

    @Test
    @DisplayName("describeOrNone() handles null, dock events and other objects")
    void describeOrNone() {
        assertEquals("B-1042 left Harbor", EventHandler.describeOrNone(new BikeUndocked("Harbor", "B-1042")),
                "A DockEvent should be passed on to describe(...)");
        assertEquals("unknown: Integer", EventHandler.describeOrNone(42),
                "Any other object should give \"unknown: \" plus getClass().getSimpleName()");
        assertEquals("no event", EventHandler.describeOrNone(null),
                "Add case null -> \"no event\" to the switch");
    }
}
