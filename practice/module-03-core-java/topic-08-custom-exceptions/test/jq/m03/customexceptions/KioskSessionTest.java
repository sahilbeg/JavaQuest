package jq.m03.customexceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KioskSessionTest {

    @Test
    @DisplayName("dock gives out dock numbers 1, 2, 3 in order")
    void docksInOrder() throws DockFullException {
        Station uptown = new Station("Uptown", 12);
        assertEquals(1, uptown.dock("B-1042"), "The first bike should get dock 1");
        assertEquals(2, uptown.dock("B-2001"), "The second bike should get dock 2");
        assertEquals(3, uptown.dock("B-3300"), "The third bike should get dock 3");
    }

    @Test
    @DisplayName("dock throws DockFullException with a clear message when the station is full")
    void fullStationThrows() throws DockFullException {
        Station harbor = new Station("Harbor", 1);
        harbor.dock("B-1042");
        DockFullException e = assertThrows(DockFullException.class, () -> harbor.dock("B-2001"),
                "Harbor has 1 dock. The second dock(...) call should throw DockFullException");
        assertEquals("Station full: Harbor", e.getMessage(),
                "The message should be \"Station full: Harbor\". Pass it to super(...) in DockFullException");
    }

    @Test
    @DisplayName("pricePerMinute knows classic, ebike and cargo")
    void knownPrices() {
        assertEquals(0.15, Pricing.pricePerMinute("classic"), 0.001, "A classic bike costs 0.15 per minute");
        assertEquals(0.30, Pricing.pricePerMinute("ebike"), 0.001, "An e-bike costs 0.30 per minute");
        assertEquals(0.25, Pricing.pricePerMinute("cargo"), 0.001, "A cargo bike costs 0.25 per minute");
    }

    @Test
    @DisplayName("pricePerMinute throws UnknownBikeTypeException for an unknown type")
    void unknownTypeThrows() {
        UnknownBikeTypeException e = assertThrows(UnknownBikeTypeException.class,
                () -> Pricing.pricePerMinute("tandem"),
                "pricePerMinute(\"tandem\") should throw UnknownBikeTypeException");
        assertEquals("Unknown bike type: tandem", e.getMessage(),
                "The message should be \"Unknown bike type: tandem\". Pass it to super(...)");
    }

    @Test
    @DisplayName("a KioskSession closes at the end of try-with-resources, even when docking fails")
    void sessionCloses() throws DockFullException {
        Station harbor = new Station("Harbor", 1);
        harbor.dock("B-1042");
        KioskSession session = new KioskSession(harbor);
        // Java 9 and later: an existing final variable can be the resource.
        try (session) {
            session.dock("B-2001");
        } catch (DockFullException e) {
            // expected: the station is full
        }
        assertTrue(session.isClosed(), "close() should set the session as closed, so isClosed() returns true");
    }

    @Test
    @DisplayName("run returns \"Docked at N\" or the station-full message")
    void runMessages() {
        Station harbor = new Station("Harbor", 2);
        assertEquals("Docked at 1", KioskSession.run(harbor, "B-1042"), "The first bike at Harbor should dock at 1");
        assertEquals("Docked at 2", KioskSession.run(harbor, "B-2001"), "The second bike at Harbor should dock at 2");
        assertEquals("Station full: Harbor", KioskSession.run(harbor, "B-3300"),
                "Harbor has 2 docks, so the third bike should get \"Station full: Harbor\"");
    }
}
