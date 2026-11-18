package jq.m09.layered;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Checks the shape of the code with reflection: who depends on whom.
class LayeringTest {

    static final List<String> WEB_PACKAGES = List.of("org.springframework.http", "org.springframework.web",
            "jakarta.servlet");

    private static boolean isWebType(Type type) {
        String name = type.getTypeName();
        return WEB_PACKAGES.stream().anyMatch(name::contains);
    }

    @Test
    @DisplayName("The controller depends only on RentalService: one constructor, one field, no storage")
    void controllerDependsOnlyOnTheService() {
        Constructor<?>[] constructors = RentalController.class.getDeclaredConstructors();
        assertEquals(1, constructors.length, "RentalController should have exactly one constructor");
        assertArrayEquals(new Class<?>[] {RentalService.class}, constructors[0].getParameterTypes(),
                "RentalController's constructor should take exactly one parameter, a RentalService");

        List<String> fields = new ArrayList<>();
        for (Field field : RentalController.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.add(field.getType().getSimpleName() + " " + field.getName());
            }
        }
        assertEquals(1, fields.size(), "RentalController should keep only the RentalService in a field. "
                + "Storage (a map, a counter) belongs in the repository. Fields found: " + fields);
        assertEquals(RentalService.class, firstInstanceField(RentalController.class).getType(),
                "RentalController's one field should be the RentalService");
    }

    private static Field firstInstanceField(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                return field;
            }
        }
        throw new AssertionError(type.getSimpleName() + " has no instance field");
    }

    @Test
    @DisplayName("No RentalService method, constructor or field uses an HTTP or servlet type")
    void serviceKnowsNothingAboutHttp() {
        List<String> leaks = new ArrayList<>();
        for (Method method : RentalService.class.getDeclaredMethods()) {
            if (isWebType(method.getGenericReturnType())) {
                leaks.add(method.getName() + " returns " + method.getGenericReturnType().getTypeName());
            }
            for (Type parameter : method.getGenericParameterTypes()) {
                if (isWebType(parameter)) {
                    leaks.add(method.getName() + " takes " + parameter.getTypeName());
                }
            }
        }
        for (Constructor<?> constructor : RentalService.class.getDeclaredConstructors()) {
            for (Type parameter : constructor.getGenericParameterTypes()) {
                if (isWebType(parameter)) {
                    leaks.add("the constructor takes " + parameter.getTypeName());
                }
            }
        }
        for (Field field : RentalService.class.getDeclaredFields()) {
            if (isWebType(field.getGenericType())) {
                leaks.add("field " + field.getName() + " is a " + field.getGenericType().getTypeName());
            }
        }
        assertTrue(leaks.isEmpty(), "The service layer must not know about HTTP. Return domain objects or DTOs "
                + "and throw domain exceptions; the controller and ApiErrors deal with HTTP. Found: " + leaks);
    }

    @Test
    @DisplayName("RentalRepository is an interface, and the service depends on it, not on the in-memory class")
    void serviceDependsOnTheRepositoryInterface() {
        assertTrue(RentalRepository.class.isInterface(), "RentalRepository should be an interface");
        assertTrue(RentalRepository.class.isAssignableFrom(InMemoryRentalRepository.class),
                "InMemoryRentalRepository should implement RentalRepository");
        Constructor<?>[] constructors = RentalService.class.getDeclaredConstructors();
        assertEquals(1, constructors.length, "RentalService should have exactly one constructor");
        assertArrayEquals(new Class<?>[] {RentalRepository.class}, constructors[0].getParameterTypes(),
                "RentalService's constructor should take one RentalRepository (the interface), "
                        + "so another storage can be plugged in later");
    }
}
