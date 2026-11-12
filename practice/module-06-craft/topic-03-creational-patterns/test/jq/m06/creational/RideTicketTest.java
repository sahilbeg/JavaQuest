package jq.m06.creational;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class RideTicketTest {

    @Test
    @DisplayName("The builder puts every value in the right slot")
    void builderHappyPath() {
        RideTicket t = RideTicket.builder()
                .bike("B-1042").from("Harbor").to("Uptown").type("cargo")
                .minutes(25).rider("r1").helmet(true).build();
        assertEquals("B-1042", t.bikeId(), "bike(\"B-1042\") should end up in bikeId");
        assertEquals("Harbor", t.fromStation(), "from(\"Harbor\") should end up in fromStation");
        assertEquals("Uptown", t.toStation(), "to(\"Uptown\") should end up in toStation, not swapped with from");
        assertEquals("cargo", t.type(), "type(\"cargo\") should end up in type");
        assertEquals(25, t.minutes(), "minutes(25) should end up in minutes");
        assertEquals("r1", t.riderId(), "rider(\"r1\") should end up in riderId");
        assertTrue(t.helmet(), "helmet(true) should end up in helmet");
    }

    @Test
    @DisplayName("Optional fields get defaults, and RideTicket.ebike builds an e-bike ticket")
    void defaultsAndStaticFactory() {
        RideTicket t = RideTicket.builder().bike("B-1042").from("Harbor").to("Uptown").minutes(25).rider("r1").build();
        assertEquals("classic", t.type(), "type should default to \"classic\" when you never call type(...)");
        assertFalse(t.helmet(), "helmet should default to false when you never call helmet(...)");

        RideTicket e = RideTicket.ebike("B-2001", "Old Mill", "Depot", 12, "r2");
        assertEquals(new RideTicket("B-2001", "Old Mill", "Depot", "ebike", 12, "r2", false), e,
                "RideTicket.ebike(\"B-2001\", \"Old Mill\", \"Depot\", 12, \"r2\") should keep from and to in order and set type \"ebike\"");
    }

    @Test
    @DisplayName("build() names the first missing required field: bike, from, to, then rider")
    void missingFields() {
        expectMissing("bike", () -> RideTicket.builder().from("Harbor").to("Uptown").rider("r1").build());
        expectMissing("from", () -> RideTicket.builder().bike("B-1042").to("Uptown").rider("r1").build());
        expectMissing("to", () -> RideTicket.builder().bike("B-1042").from("Harbor").rider("r1").build());
        expectMissing("rider", () -> RideTicket.builder().bike("B-1042").from("Harbor").to("Uptown").build());
        expectMissing("bike", () -> RideTicket.builder().build());
    }

    @Test
    @DisplayName("build() rejects negative minutes")
    void negativeMinutes() {
        assertThrows(IllegalStateException.class,
                () -> RideTicket.builder().bike("B-1042").from("Harbor").to("Uptown").rider("r1").minutes(-1).build(),
                "minutes(-1) should make build() throw an IllegalStateException");
    }

    @Test
    @DisplayName("BikeFactory.create picks the right class for each type")
    void bikeFactory() {
        assertInstanceOf(ClassicBike.class, BikeFactory.create("classic"), "create(\"classic\") should return a ClassicBike");
        assertInstanceOf(EBike.class, BikeFactory.create("ebike"), "create(\"ebike\") should return an EBike");
        assertInstanceOf(CargoBike.class, BikeFactory.create("cargo"), "create(\"cargo\") should return a CargoBike");
        assertThrows(IllegalArgumentException.class, () -> BikeFactory.create("scooter"),
                "create(\"scooter\") should throw an IllegalArgumentException, because Spoke has no scooters");
    }

    @Test
    @DisplayName("PriceTable is an enum with exactly one constant, INSTANCE")
    void priceTableSingleton() {
        assertTrue(PriceTable.class.isEnum(), "PriceTable should be declared as an enum, so nobody can call new PriceTable()");
        assertEquals(1, PriceTable.class.getEnumConstants().length, "PriceTable should have exactly one constant, INSTANCE");
        assertEquals(30, PriceTable.INSTANCE.centsPerMinute("ebike"), "an e-bike costs 30 cents a minute");
        assertEquals(15, PriceTable.INSTANCE.centsPerMinute("classic"), "a classic bike costs 15 cents a minute");
        assertEquals(25, PriceTable.INSTANCE.centsPerMinute("cargo"), "a cargo bike costs 25 cents a minute");
    }

    private static void expectMissing(String field, Executable build) {
        IllegalStateException e = assertThrows(IllegalStateException.class, build,
                "build() should throw an IllegalStateException when " + field + " is missing");
        String message = String.valueOf(e.getMessage());
        assertTrue(message.contains(field),
                "the message should name the missing field \"" + field + "\", but it was: " + message);
    }
}
