package jq.m10.capstone;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// The shape of the hexagon: the core names no framework, no SQL and no adapter, and has no city if-statements.
class CoreRulesTest {

    static final List<Class<?>> CORE = List.of(BikeType.class, BikeStatus.class, Bike.class, CityConfig.class,
            FareCalculator.class, Rental.class, RideEnded.class, StationAvailability.class, RentalException.class,
            RentalUseCases.class, RentalRepository.class, BikeInventory.class, EventPublisher.class,
            RentalService.class);

    static final Pattern FORBIDDEN = Pattern.compile(
            "org/springframework/|java/sql/|javax/sql/|jakarta/|jq/m10/capstone/(InMemory\\w*|Recording\\w*|CapstoneDemo)");

    /** The raw bytes of a compiled class, as text, so type names used inside method bodies show up too. */
    private static String bytesOf(Class<?> type) throws IOException {
        String file = type.getName().substring(type.getPackageName().length() + 1) + ".class";
        try (InputStream in = type.getResourceAsStream(file)) {
            if (in == null) {
                throw new AssertionError("Could not read " + file);
            }
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }

    @Test
    @DisplayName("No core class mentions Spring, java.sql, jakarta or an adapter, even inside a method")
    void coreIsFrameworkFree() throws IOException {
        List<String> leaks = new ArrayList<>();
        for (Class<?> core : CORE) {
            List<Class<?>> classes = new ArrayList<>(List.of(core));
            classes.addAll(List.of(core.getDeclaredClasses()));
            for (Class<?> type : classes) {
                Matcher m = FORBIDDEN.matcher(bytesOf(type));
                while (m.find()) {
                    String leak = type.getSimpleName() + " mentions " + m.group().replace('/', '.');
                    if (!leaks.contains(leak)) {
                        leaks.add(leak);
                    }
                }
            }
        }
        assertTrue(leaks.isEmpty(), "The core must stay plain Java and talk only to its ports. "
                + "Frameworks, SQL and adapters belong outside it. Found: " + leaks);
    }

    @Test
    @DisplayName("RentalService takes one city's config, its ports and a Clock, and has no city names in its code")
    void configNotIfStatements() throws IOException {
        assertTrue(RentalUseCases.class.isAssignableFrom(RentalService.class),
                "RentalService should implement RentalUseCases, the driving port");
        Constructor<?>[] constructors = RentalService.class.getDeclaredConstructors();
        assertEquals(1, constructors.length, "RentalService should have exactly one constructor");
        assertArrayEquals(new Class<?>[] {CityConfig.class, RentalRepository.class, BikeInventory.class,
                        EventPublisher.class, Clock.class}, constructors[0].getParameterTypes(),
                "RentalService(CityConfig, RentalRepository, BikeInventory, EventPublisher, Clock)");
        for (Class<?> type : List.of(RentalService.class, FareCalculator.class)) {
            String code = bytesOf(type).toLowerCase(Locale.ROOT);
            assertTrue(!code.contains("bramford") && !code.contains("riverton"),
                    type.getSimpleName() + " should not name a city. Read prices, stations and the zone from "
                            + "CityConfig instead of writing if (city is Bramford)");
        }
    }
}
