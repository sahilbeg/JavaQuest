package jq.m05.sealedtypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventLogTest {

    @Test
    @DisplayName("DockEvent is sealed and permits exactly the three dock records")
    void dockEventIsSealed() {
        assertTrue(DockEvent.class.isSealed(),
                "DockEvent should be declared as: public sealed interface DockEvent permits ...");
        Class<?>[] permitted = DockEvent.class.getPermittedSubclasses();
        Set<Class<?>> actual = Arrays.stream(permitted).collect(Collectors.toSet());
        assertEquals(Set.of(BikeDocked.class, BikeUndocked.class, Fault.class), actual,
                "DockEvent should permit exactly BikeDocked, BikeUndocked and Fault");
    }

    @Test
    @DisplayName("describe() writes a docked message")
    void describeDocked() {
        assertEquals("B-1042 docked at Harbor", EventLog.describe(new BikeDocked("Harbor", "B-1042")),
                "A BikeDocked event should read: <bikeId> docked at <station>");
    }

    @Test
    @DisplayName("describe() writes an undocked message")
    void describeUndocked() {
        assertEquals("B-1042 left Harbor", EventLog.describe(new BikeUndocked("Harbor", "B-1042")),
                "A BikeUndocked event should read: <bikeId> left <station>");
    }

    @Test
    @DisplayName("describe() writes a fault message")
    void describeFault() {
        assertEquals("Fault E7 at Uptown", EventLog.describe(new Fault("Uptown", "E7")),
                "A Fault event should read: Fault <code> at <station>");
    }

    @Test
    @DisplayName("freeDocksChange() adds up docks freed and taken")
    void freeDocksChange() {
        List<DockEvent> events = List.of(
                new BikeUndocked("Harbor", "B-1042"),
                new BikeUndocked("Harbor", "B-2001"),
                new Fault("Harbor", "E7"),
                new BikeUndocked("Harbor", "B-3003"),
                new BikeDocked("Harbor", "B-0007"));
        assertEquals(2, EventLog.freeDocksChange(events),
                "3 undocks (+3), 1 dock (-1) and 1 fault (0) should give 2 free docks");
        assertEquals(0, EventLog.freeDocksChange(List.of()),
                "An empty list of events should change nothing, so the answer is 0");
    }
}
