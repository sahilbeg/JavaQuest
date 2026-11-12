package jq.m02.abstractclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VehicleTest {

    @Test
    @DisplayName("Vehicle stays abstract so nobody can make a plain Vehicle")
    void vehicleIsAbstract() {
        assertTrue(Modifier.isAbstract(Vehicle.class.getModifiers()),
                "Vehicle should be declared public abstract class Vehicle, so new Vehicle(...) cannot compile");
    }

    @Test
    @DisplayName("Each bike type reports its own price per minute")
    void pricesPerMinute() {
        assertEquals(0.15, new ClassicBike("B-1001").pricePerMinute(), 0.001,
                "ClassicBike.pricePerMinute() should return 0.15");
        assertEquals(0.30, new EBike("B-1042").pricePerMinute(), 0.001,
                "EBike.pricePerMinute() should return 0.30");
        assertEquals(0.25, new CargoBike("B-2001").pricePerMinute(), 0.001,
                "CargoBike.pricePerMinute() should return 0.25");
    }

    @Test
    @DisplayName("Each bike type reports its kind")
    void kinds() {
        assertEquals("classic", new ClassicBike("B-1001").kind(), "ClassicBike.kind() should return \"classic\"");
        assertEquals("ebike", new EBike("B-1042").kind(), "EBike.kind() should return \"ebike\"");
        assertEquals("cargo", new CargoBike("B-2001").kind(), "CargoBike.kind() should return \"cargo\"");
    }

    @Test
    @DisplayName("fare adds the unlock fee to the minutes times the bike's price")
    void fares() {
        assertEquals(2.5, new ClassicBike("B-1001").fare(10), 0.001,
                "ClassicBike fare(10) should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
        assertEquals(7.0, new EBike("B-1042").fare(20), 0.001,
                "EBike fare(20) should be 7.0: unlock fee 1.00 plus 20 minutes at 0.30");
        assertEquals(1.0, new CargoBike("B-2001").fare(0), 0.001,
                "CargoBike fare(0) should be 1.0: just the unlock fee");
    }

    @Test
    @DisplayName("A Vehicle variable holding a CargoBike uses the cargo price")
    void fareThroughVehicleVariable() {
        Vehicle v = new CargoBike("B-2001");
        assertEquals(4.75, v.fare(15), 0.001,
                "fare(15) on a CargoBike should be 4.75: unlock fee 1.00 plus 15 minutes at 0.25");
    }

    @Test
    @DisplayName("label shows the kind, a space and the id")
    void labels() {
        assertEquals("ebike B-1042", new EBike("B-1042").label(),
                "label() should return kind() + \" \" + id, so EBike B-1042 gives \"ebike B-1042\"");
        assertEquals("classic B-1001", new ClassicBike("B-1001").label(),
                "label() for ClassicBike B-1001 should be \"classic B-1001\"");
    }
}
