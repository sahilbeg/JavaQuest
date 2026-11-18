package jq.m09.hexagonal;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Checks the dependency rule of the hexagon: the core never names an adapter or an outside technology.
class HexagonRulesTest {

    static final List<Class<?>> CORE = List.of(BikeType.class, Rental.class, PaymentResult.class, RideReceipt.class,
            RideNotFoundException.class, RideAlreadyEndedException.class, EndRideUseCase.class, RentalStore.class,
            PaymentPort.class, RiderNotifier.class, EndRideService.class);

    /** Names the core must never mention: PayRiver's types, adapters, the composition root and outside technologies. */
    static final Pattern FORBIDDEN = Pattern.compile(
            "jq/m09/hexagonal/(PayRiver\\w*|\\w*Adapter|InMemory\\w*|SpokeRides)|java/sql/|org/springframework/|jakarta/");

    /** Every type name a compiled class mentions, found in its .class file, including inside method bodies. */
    private static List<String> forbiddenNamesIn(Class<?> type) throws IOException {
        String file = type.getSimpleName() + ".class";
        try (InputStream in = type.getResourceAsStream(file)) {
            if (in == null) {
                throw new AssertionError("Could not read " + file);
            }
            String bytes = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            List<String> found = new ArrayList<>();
            Matcher m = FORBIDDEN.matcher(bytes);
            while (m.find()) {
                if (!found.contains(m.group())) {
                    found.add(m.group());
                }
            }
            return found;
        }
    }

    @Test
    @DisplayName("No core class mentions PayRiver, an adapter, java.sql, Spring or jakarta, even inside a method")
    void coreKnowsNoAdapters() throws IOException {
        List<String> leaks = new ArrayList<>();
        for (Class<?> core : CORE) {
            List<Class<?>> classes = new ArrayList<>(List.of(core));
            classes.addAll(List.of(core.getDeclaredClasses()));
            for (Class<?> type : classes) {
                for (String name : forbiddenNamesIn(type)) {
                    leaks.add(type.getSimpleName() + " mentions " + name.replace('/', '.'));
                }
            }
        }
        assertTrue(leaks.isEmpty(), "The core must depend only on its own ports. PayRiver's formats and status codes "
                + "belong in PayRiverPaymentAdapter. Found: " + leaks);
    }

    @Test
    @DisplayName("EndRideService implements the driving port and takes only driven ports and a Clock")
    void serviceDependsOnPortsOnly() {
        assertTrue(EndRideUseCase.class.isAssignableFrom(EndRideService.class),
                "EndRideService should implement EndRideUseCase, the driving port");
        Constructor<?>[] constructors = EndRideService.class.getDeclaredConstructors();
        assertEquals(1, constructors.length, "EndRideService should have exactly one constructor");
        assertArrayEquals(new Class<?>[] {RentalStore.class, PaymentPort.class, RiderNotifier.class, Clock.class},
                constructors[0].getParameterTypes(),
                "EndRideService's constructor should take (RentalStore, PaymentPort, RiderNotifier, Clock)");
        for (Class<?> port : List.of(EndRideUseCase.class, RentalStore.class, PaymentPort.class, RiderNotifier.class)) {
            assertTrue(port.isInterface(), port.getSimpleName() + " is a port, so it should be an interface");
        }
        assertTrue(PaymentPort.class.isAssignableFrom(PayRiverPaymentAdapter.class),
                "PayRiverPaymentAdapter should implement PaymentPort, the port the core owns");
        assertTrue(RentalStore.class.isAssignableFrom(InMemoryRentalStore.class),
                "InMemoryRentalStore should implement RentalStore");
    }
}
