package jq.m06.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.DayOfWeek;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class PromoEngineTest {

    private static final List<Promo> ALL = List.of(new SundayHalfPrice(), new EbikeTwentyOff(), new TenthRideFree());

    private final Logger engineLogger = (Logger) LoggerFactory.getLogger(PromoEngine.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

    @BeforeEach
    void captureLogs() {
        logs.start();
        engineLogger.addAppender(logs);
    }

    @AfterEach
    void stopCapturing() {
        engineLogger.detachAppender(logs);
    }

    private static RideContext ride(String type, DayOfWeek day, int ridesThisMonth) {
        return new RideContext("r1", type, 15, day, ridesThisMonth);
    }

    @Test
    @DisplayName("each promo applies only to its own rides")
    void eachPromo() {
        assertTrue(new SundayHalfPrice().appliesTo(ride("classic", DayOfWeek.SUNDAY, 0)), "Sunday ride");
        assertEquals(false, new SundayHalfPrice().appliesTo(ride("classic", DayOfWeek.SATURDAY, 0)), "Saturday is not Sunday");
        assertEquals(112, new SundayHalfPrice().discountCents(ride("classic", DayOfWeek.SUNDAY, 0), 225), "half of 225, rounded down");
        assertTrue(new EbikeTwentyOff().appliesTo(ride("ebike", DayOfWeek.MONDAY, 0)), "ebike ride");
        assertEquals(false, new EbikeTwentyOff().appliesTo(ride("cargo", DayOfWeek.MONDAY, 0)), "cargo is not an ebike");
        assertEquals(46, new EbikeTwentyOff().discountCents(ride("ebike", DayOfWeek.MONDAY, 0), 233), "20 percent of 233 is 46.6, rounded down");
        assertTrue(new TenthRideFree().appliesTo(ride("classic", DayOfWeek.MONDAY, 19)), "19 earlier rides: this is the 20th");
        assertEquals(false, new TenthRideFree().appliesTo(ride("classic", DayOfWeek.MONDAY, 10)), "10 earlier rides: this is the 11th");
        assertEquals(250, new TenthRideFree().discountCents(ride("classic", DayOfWeek.MONDAY, 9), 250), "the whole fare");
    }

    @Test
    @DisplayName("the engine picks the single best promo")
    void picksBest() {
        PromoEngine engine = new PromoEngine(ALL);
        assertEquals(new Quote(850, 425, 425, "sunday-half-price"), engine.best(ride("ebike", DayOfWeek.SUNDAY, 3), 850),
                "Sunday half price (425) beats ebike 20 percent (170); promos do not stack");
        assertEquals(new Quote(250, 250, 0, "tenth-ride-free"), engine.best(ride("ebike", DayOfWeek.SUNDAY, 9), 250),
                "the free 10th ride beats both");
        assertEquals(new Quote(350, 0, 350, "none"), engine.best(ride("cargo", DayOfWeek.MONDAY, 2), 350),
                "no promo applies to a Monday cargo ride");
    }

    @Test
    @DisplayName("ties go to the name that comes first, and discounts never exceed the fare")
    void tiesAndCap() {
        Promo flatFive = new FixedPromo("flat-500", 500);
        Promo alsoFive = new FixedPromo("alpha-500", 500);
        PromoEngine engine = new PromoEngine(List.of(flatFive, alsoFive));
        assertEquals(new Quote(300, 300, 0, "alpha-500"), engine.best(ride("classic", DayOfWeek.MONDAY, 0), 300),
                "both promos offer 500 cents, capped at the 300-cent fare; alpha-500 comes first alphabetically");
    }

    @Test
    @DisplayName("a new promo plugs in without changing the engine")
    void pluggable() {
        PromoEngine engine = new PromoEngine(List.of(new SundayHalfPrice(), new FixedPromo("rain-day", 600)));
        assertEquals("rain-day", engine.best(ride("classic", DayOfWeek.SUNDAY, 0), 1000).promoName(),
                "a promo class the engine has never seen still competes: 600 beats half of 1000");
    }

    @Test
    @DisplayName("every used promo is logged once at INFO, and no promo means no log")
    void logging() {
        PromoEngine engine = new PromoEngine(ALL);
        engine.best(new RideContext("r7", "classic", 15, DayOfWeek.SUNDAY, 0), 450);
        engine.best(new RideContext("r8", "cargo", 15, DayOfWeek.MONDAY, 0), 450);
        assertEquals(1, logs.list.size(), "one log line for the Sunday ride, none for the Monday cargo ride");
        ILoggingEvent line = logs.list.getFirst();
        assertEquals(Level.INFO, line.getLevel(), "a used promo is normal business: INFO");
        assertEquals("promo sunday-half-price saved 225 cents for r7", line.getFormattedMessage(), "the log message format");
    }

    private record FixedPromo(String name, long cents) implements Promo {

        @Override
        public boolean appliesTo(RideContext ride) {
            return true;
        }

        @Override
        public long discountCents(RideContext ride, long fareCents) {
            return cents;
        }
    }
}
