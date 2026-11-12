package jq.m02.inheritance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeTest {

    @Test
    @DisplayName("A classic bike charges 0.15 per minute plus the unlock fee")
    void classicBike() {
        Bike bike = new Bike("B-1042");
        assertEquals("B-1042", bike.getId(), "getId() should return the id passed to the constructor");
        assertEquals(2.5, bike.fare(10), 0.001,
                "fare(10) should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
    }

    @Test
    @DisplayName("An e-bike keeps its id and its battery level")
    void eBikeFields() {
        EBike bike = new EBike("B-2001", 80);
        assertEquals("B-2001", bike.getId(),
                "getId() on an EBike should return \"B-2001\". Pass the id up with super(id)");
        assertEquals(80, bike.getBattery(),
                "getBattery() should return 80. Store the battery parameter in the field (TODO 1)");
    }

    @Test
    @DisplayName("An e-bike charges 0.30 per minute")
    void eBikePrice() {
        EBike bike = new EBike("B-2001", 80);
        assertEquals(0.30, bike.pricePerMinute(), 0.001,
                "EBike.pricePerMinute() should be 0.30. Override it in EBike (TODO 2)");
        assertEquals(4.0, bike.fare(10), 0.001,
                "EBike fare(10) should be 4.0: unlock fee 1.00 plus 10 minutes at 0.30");
    }

    @Test
    @DisplayName("A cargo bike charges 0.25 per minute")
    void cargoPrice() {
        CargoBike bike = new CargoBike("B-3005");
        assertEquals(0.25, bike.pricePerMinute(), 0.001,
                "CargoBike.pricePerMinute() should be 0.25. Override it in CargoBike (TODO 3)");
        assertEquals(3.5, bike.fare(10), 0.001,
                "CargoBike fare(10) should be 3.5: unlock fee 1.00 plus 10 minutes at 0.25");
    }

    @Test
    @DisplayName("A cargo bike adds [cargo] to the description from Bike")
    void cargoDescribe() {
        CargoBike bike = new CargoBike("B-1042");
        assertEquals("Bike B-1042 [cargo]", bike.describe(),
                "describe() should return \"Bike B-1042 [cargo]\". Use super.describe() + \" [cargo]\" (TODO 4)");
    }
}
