package fx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdderTest {
    @Test
    @DisplayName("add puts two numbers together")
    void adds() {
        assertEquals(5, Adder.add(2, 3), "add(2, 3) should be 5");
    }

    @Test
    @DisplayName("add works with negative numbers")
    void addsNegatives() {
        assertEquals(-1, Adder.add(2, -3), "add(2, -3) should be -1");
    }
}
