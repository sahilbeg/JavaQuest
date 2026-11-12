package jq.m00.helloworld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HelloTest {

    @Test
    @DisplayName("greeting() returns exactly Hello, World!")
    void greetingText() {
        assertEquals("Hello, World!", Hello.greeting(),
                "greeting() should return \"Hello, World!\". Check the capital letters, the comma and the !");
    }

    @Test
    @DisplayName("main prints the greeting on its own line")
    void mainPrintsGreeting() {
        PrintStream original = System.out;
        var captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true));
        try {
            Hello.main(new String[0]);
        } finally {
            System.setOut(original);
        }
        assertEquals("Hello, World!", captured.toString().strip(),
                "main should print Hello, World! using System.out.println");
    }
}
