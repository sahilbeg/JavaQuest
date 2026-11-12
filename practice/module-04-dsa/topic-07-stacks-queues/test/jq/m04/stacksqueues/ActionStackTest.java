package jq.m04.stacksqueues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActionStackTest {

    @Test
    @DisplayName("Undo takes back the newest action first")
    void stackIsLastInFirstOut() {
        ActionStack stack = new ActionStack();
        assertTrue(stack.isEmpty(), "A new ActionStack should be empty: isEmpty() should return true");
        stack.push("log in");
        stack.push("scan card");
        stack.push("select bike");
        assertEquals(3, stack.size(), "After 3 pushes, size() should be 3");
        assertEquals("select bike", stack.peek(), "peek() should show the newest action, select bike");
        assertEquals(3, stack.size(), "peek() must not remove anything: size() should still be 3");
        assertEquals("select bike", stack.pop(), "pop() should return the newest action first");
        assertEquals("scan card", stack.pop(), "The second pop() should return scan card");
        assertEquals("log in", stack.pop(), "The last pop() should return the oldest action, log in");
        assertTrue(stack.isEmpty(), "After popping everything, isEmpty() should be true");
    }

    @Test
    @DisplayName("The stack grows past its starting array and keeps every action")
    void stackGrows() {
        ActionStack stack = new ActionStack();
        for (int i = 1; i <= 20; i++) {
            stack.push("action " + i);
        }
        assertEquals(20, stack.size(), "After 20 pushes, size() should be 20. Double the array when it is full");
        for (int i = 20; i >= 1; i--) {
            assertEquals("action " + i, stack.pop(),
                    "Pops should come back newest first. Did you copy every old item into the bigger array?");
        }
        assertTrue(stack.isEmpty(), "After 20 pops the stack should be empty");
    }

    @Test
    @DisplayName("Popping an empty stack throws IllegalStateException and peek returns null")
    void emptyStack() {
        ActionStack stack = new ActionStack();
        assertNull(stack.peek(), "peek() on an empty stack should return null");
        try {
            stack.pop();
            fail("pop() on an empty stack should throw IllegalStateException");
        } catch (IllegalStateException expected) {
            // this is what the kiosk wants
        }
    }

    @Test
    @DisplayName("ActionStack is built on a String array, with no java.util collection inside")
    void builtByHand() {
        boolean hasArray = false;
        for (Field field : ActionStack.class.getDeclaredFields()) {
            assertFalse(field.getType().getName().startsWith("java.util"),
                    "ActionStack should not use " + field.getType().getSimpleName() + ". Build it on a String[]");
            if (field.getType() == String[].class) {
                hasArray = true;
            }
        }
        assertTrue(hasArray, "ActionStack should keep its actions in a String[] field");
    }
}
