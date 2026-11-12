package jq.m04.stacksqueues;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BracketsTest {

    @Test
    @DisplayName("Brackets.balanced spots matched and mismatched brackets")
    void brackets() {
        assertTrue(Brackets.balanced("{[()]}"), "{[()]} is balanced: every bracket closes in the right order");
        assertTrue(Brackets.balanced("dock(Harbor)[12]"), "Letters are skipped, so dock(Harbor)[12] is balanced");
        assertTrue(Brackets.balanced(""), "Empty text has no brackets, so it is balanced");
        assertFalse(Brackets.balanced("([)]"), "([)] closes ( before [, so it is not balanced");
        assertFalse(Brackets.balanced("(("), "(( never closes, so it is not balanced. Check the stack is empty at the end");
        assertFalse(Brackets.balanced("())"), "()) has a ) with nothing to close, so it is not balanced");
    }
}
