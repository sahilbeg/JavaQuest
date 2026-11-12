package jq.m04.treesbst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeRegistryTest {

    private static BikeRegistry registryOf(int... bikes) {
        BikeRegistry registry = new BikeRegistry();
        for (int bike : bikes) {
            registry.insert(bike);
        }
        return registry;
    }

    @Test
    @DisplayName("inOrder lists the bikes in id order and ignores a duplicate")
    void inOrderIsSorted() {
        BikeRegistry registry = registryOf(1042, 1017, 1088, 1003, 1025, 1042, 1061);
        assertEquals(List.of(1003, 1017, 1025, 1042, 1061, 1088), registry.inOrder(),
                "inOrder() should visit left subtree, node, right subtree, giving ids smallest first");
        assertEquals(6, registry.size(),
                "size() should be 6: seven inserts, but 1042 arrived twice and the second one is ignored");
    }

    @Test
    @DisplayName("contains finds bikes that were added and rejects bikes that were not")
    void containsWorks() {
        BikeRegistry registry = registryOf(1042, 1017, 1088, 1025);
        assertTrue(registry.contains(1042), "contains(1042) should be true: 1042 is the root");
        assertTrue(registry.contains(1025), "contains(1025) should be true: go left at 1042, then right at 1017");
        assertFalse(registry.contains(1050), "contains(1050) should be false: the search falls off the tree at null");
        assertFalse(new BikeRegistry().contains(1042), "contains on an empty registry should be false");
    }

    @Test
    @DisplayName("height is 0 when empty, 1 for one bike, and 3 for a balanced seven-bike tree")
    void heightOfBalancedTree() {
        assertEquals(0, new BikeRegistry().height(), "An empty registry has height 0");
        assertEquals(1, registryOf(1042).height(), "A registry with one bike has height 1");
        BikeRegistry balanced = registryOf(1042, 1017, 1088, 1003, 1025, 1061, 1099);
        assertEquals(3, balanced.height(),
                "These seven bikes form a full tree: root, 2 children, 4 leaves, so height 3");
    }

    @Test
    @DisplayName("bikes arriving in sorted order make a degenerate tree as tall as its size")
    void sortedArrivalsDegenerate() {
        BikeRegistry registry = registryOf(1001, 1002, 1003, 1004, 1005);
        assertEquals(5, registry.height(),
                "Each new bike is larger, so it always goes right: 5 bikes make a chain of height 5");
        assertEquals(List.of(1001, 1002, 1003, 1004, 1005), registry.inOrder(),
                "inOrder() should still list the chain in id order");
    }

    @Test
    @DisplayName("min and max return the smallest and largest bike")
    void minAndMax() {
        BikeRegistry registry = registryOf(1042, 1017, 1088, 1003, 1099);
        assertEquals(1003, registry.min(), "min() should keep going left until there is no left child: 1003");
        assertEquals(1099, registry.max(), "max() should keep going right until there is no right child: 1099");
    }

    @Test
    @DisplayName("min and max throw NoSuchElementException on an empty registry")
    void emptyMinMaxThrow() {
        BikeRegistry empty = new BikeRegistry();
        assertThrows(NoSuchElementException.class, empty::min,
                "min() on an empty registry should throw NoSuchElementException");
        assertThrows(NoSuchElementException.class, empty::max,
                "max() on an empty registry should throw NoSuchElementException");
    }

    @Test
    @DisplayName("BikeRegistry is built from its own nodes, with no java.util collection inside")
    void builtByHand() {
        for (Field field : BikeRegistry.class.getDeclaredFields()) {
            assertFalse(field.getType().getName().startsWith("java.util"),
                    "BikeRegistry should not use " + field.getType().getSimpleName() + ". Link your own Node objects");
        }
    }
}
