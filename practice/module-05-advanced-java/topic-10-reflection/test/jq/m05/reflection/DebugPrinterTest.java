package jq.m05.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DebugPrinterTest {

    @Test
    @DisplayName("describe lists every instance field of a Bike in order")
    void describeBike() {
        assertEquals("Bike{id=B-1042, battery=80}", DebugPrinter.describe(new Bike("B-1042", 80)),
                "describe should print the simple class name, then name=value for each field, joined with \", \"."
                        + " The static field bikesBuilt must be skipped.");
    }

    @Test
    @DisplayName("describe works on a different class without changes")
    void describeStation() {
        assertEquals("Station{name=Harbor, docks=12, bikesDocked=7}",
                DebugPrinter.describe(new Station("Harbor", 12, 7)),
                "describe should read the fields of any object, so a Station shows name, docks and bikesDocked");
    }

    @Test
    @DisplayName("methodNames gives the public declared methods, sorted")
    void methodNamesSorted() {
        assertEquals(List.of("getBattery", "getId", "isLow"), DebugPrinter.methodNames(Bike.class),
                "methodNames(Bike.class) should keep only public methods (secretCode is private) and sort them A to Z");
    }

    @Test
    @DisplayName("create builds an object from its full class name")
    void createStation() {
        Object made = DebugPrinter.create("jq.m05.reflection.Station");
        assertTrue(made instanceof Station,
                "create(\"jq.m05.reflection.Station\") should return a new Station built with its no-arg constructor");
        assertEquals("Station{name=Riverside, docks=12, bikesDocked=0}", DebugPrinter.describe(made),
                "the no-arg Station constructor sets Riverside, 12 docks and 0 bikes");
    }

    @Test
    @DisplayName("create throws IllegalArgumentException for an unknown class")
    void createUnknown() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> DebugPrinter.create("jq.m05.reflection.Unicycle"),
                "create should catch ClassNotFoundException and throw IllegalArgumentException instead");
        assertTrue(error.getMessage() != null && error.getMessage().contains("Unicycle"),
                "the message should name the class that was not found, so support can see the typo");
    }

    @Test
    @DisplayName("callNoArg runs a method chosen by its name")
    void callNoArgRunsMethod() {
        Bike bike = new Bike("B-1042", 15);
        assertEquals(true, DebugPrinter.callNoArg(bike, "isLow"),
                "callNoArg(bike, \"isLow\") should invoke isLow() and return its result: 15 percent battery is low");
        assertEquals("B-1042", DebugPrinter.callNoArg(bike, "getId"),
                "callNoArg(bike, \"getId\") should return the bike id");
    }
}
