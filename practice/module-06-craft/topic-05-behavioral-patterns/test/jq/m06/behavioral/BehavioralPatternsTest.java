package jq.m06.behavioral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BehavioralPatternsTest {

    @Test
    @DisplayName("StandardPricing charges 100 cents to unlock plus 15 cents a minute")
    void standardPricing() {
        PricingStrategy standard = new StandardPricing();
        assertEquals(100, standard.priceCents(0),
                "StandardPricing.priceCents(0) should be 100: just the unlock fee");
        assertEquals(250, standard.priceCents(10),
                "StandardPricing.priceCents(10) should be 250: 100 unlock plus 10 minutes at 15");
        assertEquals(550, standard.priceCents(30),
                "StandardPricing.priceCents(30) should be 550: 100 unlock plus 30 minutes at 15");
    }

    @Test
    @DisplayName("RainDiscount halves whatever strategy it wraps, rounding down")
    void rainDiscount() {
        assertEquals(125, new RainDiscount(new StandardPricing()).priceCents(10),
                "Rain discount on a 10 minute standard ride should be 125: half of 250");
        assertEquals(57, new RainDiscount(new StandardPricing()).priceCents(1),
                "Rain discount on a 1 minute standard ride should be 57: half of 115, rounded down");
        assertEquals(3, new RainDiscount(minutes -> 7).priceCents(5),
                "RainDiscount must ask the strategy it wraps. Wrapping a lambda that always says 7 should give 3");
    }

    @Test
    @DisplayName("Surge multiplies whatever strategy it wraps, rounding down")
    void surge() {
        assertEquals(375, new Surge(new StandardPricing(), 1.5).priceCents(10),
                "A 1.5 surge on a 10 minute standard ride should be 375: 250 times 1.5");
        assertEquals(151, new Surge(minutes -> 101, 1.5).priceCents(3),
                "Surge must ask the strategy it wraps and round down. 101 times 1.5 is 151.5, so expect 151");
        assertEquals(250, new Surge(new RainDiscount(new StandardPricing()), 2.0).priceCents(10),
                "Strategies should stack: a 2.0 surge on a rain discount of 250 should be back to 250");
    }

    @Test
    @DisplayName("RideEvents tells every subscriber in order, and unsubscribing stops the messages")
    void rideEvents() {
        RideEvents events = new RideEvents();
        List<String> heard = new ArrayList<>();
        events.subscribe(bikeId -> heard.add("app " + bikeId));
        Runnable stopEmail = events.subscribe(bikeId -> heard.add("email " + bikeId));
        events.subscribe(bikeId -> heard.add("dashboard " + bikeId));

        events.rideEnded("B-1042");
        assertEquals(List.of("app B-1042", "email B-1042", "dashboard B-1042"), heard,
                "rideEnded should call every subscriber once, in the order they subscribed");

        stopEmail.run();
        heard.clear();
        events.rideEnded("B-2001");
        assertEquals(List.of("app B-2001", "dashboard B-2001"), heard,
                "After running the Runnable from subscribe, that subscriber should hear nothing more");
    }

    @Test
    @DisplayName("SetDocks changes the docks and undo puts the old number back")
    void setDocks() {
        Station harbor = new Station("Harbor", 12);
        AdminCommand command = new SetDocks(harbor, 8);
        command.execute();
        assertEquals(8, harbor.docks(), "execute() should set Harbor to 8 docks");
        command.undo();
        assertEquals(12, harbor.docks(), "undo() should put Harbor back to the 12 docks it had before");
    }

    @Test
    @DisplayName("CommandHistory runs commands and undoes them newest first")
    void commandHistory() {
        Station uptown = new Station("Uptown", 12);
        CommandHistory history = new CommandHistory();
        assertFalse(history.undo(), "undo() on an empty history should return false and do nothing");

        history.run(new SetDocks(uptown, 10));
        history.run(new SetDocks(uptown, 6));
        assertEquals(6, uptown.docks(), "run() should execute each command, so Uptown should now have 6 docks");
        assertEquals(2, history.size(), "After running two commands, size() should be 2");

        assertTrue(history.undo(), "undo() should return true when it undid a command");
        assertEquals(10, uptown.docks(), "The first undo should reverse the newest command, back to 10 docks");
        assertEquals(1, history.size(), "After one undo, size() should be 1");

        assertTrue(history.undo(), "The second undo should also return true");
        assertEquals(12, uptown.docks(), "The second undo should bring Uptown back to 12 docks");
        assertFalse(history.undo(), "A third undo has nothing left, so it should return false");
        assertEquals(0, history.size(), "size() should be 0 once everything is undone");
    }
}
