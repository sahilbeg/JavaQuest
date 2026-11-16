package jq.m08.di;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FareServiceTest {

    static final String MONDAY_MORNING = "2026-03-02T09:00:00Z";
    static final String SUNDAY_MORNING = "2026-03-01T09:00:00Z";

    /** A clock the tests can move to any moment. Spring uses it instead of the real clock, because it is @Primary. */
    static class TestClock extends Clock {
        private Instant now = Instant.parse(MONDAY_MORNING);

        void setTo(String isoInstant) {
            now = Instant.parse(isoInstant);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        TestClock testClock() {
            return new TestClock();
        }
    }

    @Autowired ApplicationContext context;
    @Autowired TestClock testClock;

    /** Asks Spring for the FareService bean, with a hint when there is none. */
    private FareService fareService() {
        FareService service = context.getBeanProvider(FareService.class).getIfAvailable();
        assertNotNull(service, "Spring has no FareService bean yet. Put @Service above the FareService class (TODO 4)");
        return service;
    }

    @Test
    @DisplayName("Spring holds exactly two price lists: the standard one and the Sunday one")
    void twoPriceListBeans() {
        Set<String> types = new TreeSet<>();
        context.getBeansOfType(PriceList.class).values().forEach(p -> types.add(p.getClass().getSimpleName()));
        assertEquals(Set.of("StandardPriceList", "SundayPriceList"), types,
                "Spring should hold one StandardPriceList bean and one SundayPriceList bean, but it holds " + types
                        + ". Put @Component above both price list classes (TODO 1 and 2)");
    }

    @Test
    @DisplayName("When nobody says which price list, Spring picks StandardPriceList")
    void standardIsPrimary() {
        PriceList chosen = context.getBeanProvider(PriceList.class).getIfUnique();
        assertInstanceOf(StandardPriceList.class, chosen,
                "Asking Spring for \"a PriceList\" should give StandardPriceList. Mark it @Primary (TODO 1)");
    }

    @Test
    @DisplayName("The qualifier \"sunday\" picks SundayPriceList")
    void sundayQualifier() {
        PriceList sunday = assertDoesNotThrow(
                () -> BeanFactoryAnnotationUtils.qualifiedBeanOfType(context, PriceList.class, "sunday"),
                "Spring found no PriceList bean for the qualifier \"sunday\". Put @Component and @Qualifier(\"sunday\") above SundayPriceList (TODO 2)");
        assertInstanceOf(SundayPriceList.class, sunday, "The qualifier \"sunday\" should pick SundayPriceList");
    }

    @Test
    @DisplayName("ClockConfig gives Spring one system clock in UTC")
    void clockConfigMakesAUtcClock() {
        assertEquals(1, context.getBeanNamesForType(ClockConfig.class).length,
                "Spring is not using ClockConfig. Put @Configuration above the class (TODO 3)");
        Map<String, Clock> clocks = new HashMap<>(context.getBeansOfType(Clock.class));
        clocks.remove("testClock");
        assertEquals(1, clocks.size(), "ClockConfig should give Spring exactly one Clock bean, but Spring found "
                + clocks.size() + ". Put @Bean above the clock() method (TODO 3)");
        Clock clock = clocks.values().iterator().next();
        assertEquals(ZoneOffset.UTC, clock.getZone(), "The clock from ClockConfig should be Clock.systemUTC()");
    }

    @Test
    @DisplayName("FareService is one shared bean: asking twice gives the same object")
    void fareServiceIsASingleton() {
        FareService first = fareService();
        FareService second = context.getBean(FareService.class);
        assertSame(first, second, "Spring should create one FareService and hand out that same object every time");
        assertEquals(1, context.getBeanNamesForType(FareService.class).length, "Spring should hold exactly one FareService bean");
    }

    @Test
    @DisplayName("On a Monday a 10-minute e-bike ride costs 400 cents")
    void mondayEbike() {
        testClock.setTo(MONDAY_MORNING);
        long fare = fareService().fareCents(BikeType.EBIKE, 10);
        assertEquals(400, fare, "Monday prices: 100 to unlock plus 30 a minute for an EBIKE, so 10 minutes cost 400 cents");
    }

    @Test
    @DisplayName("On a Monday classic and cargo bikes use the standard prices")
    void mondayClassicAndCargo() {
        testClock.setTo(MONDAY_MORNING);
        FareService service = fareService();
        assertEquals(400, service.fareCents(BikeType.CLASSIC, 20),
                "Monday prices: 100 to unlock plus 15 a minute for a CLASSIC, so 20 minutes cost 400 cents");
        assertEquals(200, service.fareCents(BikeType.CARGO, 4),
                "Monday prices: 100 to unlock plus 25 a minute for a CARGO, so 4 minutes cost 200 cents");
    }

    @Test
    @DisplayName("On a Sunday a 10-minute e-bike ride costs 200 cents")
    void sundayEbike() {
        testClock.setTo(SUNDAY_MORNING);
        long fare = fareService().fareCents(BikeType.EBIKE, 10);
        assertEquals(200, fare, "Sunday prices: 50 to unlock plus 15 a minute for an EBIKE, so 10 minutes cost 200 cents."
                + " Does FareService use the Clock and the \"sunday\" price list that Spring hands in (TODO 5 and 6)?");
    }

    @Test
    @DisplayName("On a Sunday classic and cargo minutes cost half, rounded down")
    void sundayClassicAndCargo() {
        testClock.setTo(SUNDAY_MORNING);
        FareService service = fareService();
        assertEquals(120, service.fareCents(BikeType.CLASSIC, 10),
                "Sunday prices: 50 to unlock plus 7 a minute for a CLASSIC, so 10 minutes cost 120 cents");
        assertEquals(170, service.fareCents(BikeType.CARGO, 10),
                "Sunday prices: 50 to unlock plus 12 a minute for a CARGO, so 10 minutes cost 170 cents");
    }
}
