package dev.javaquest.practice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class CompilerHelpTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
        "';' expected                                              | expected a ;",
        "cannot find symbol\\n  symbol:   variable nmae              | doesn't know this name",
        "incompatible types: String cannot be converted to int     | a String can't go where a int is needed",
        "incompatible types: possible lossy conversion from double to int | Add a cast like (int)",
        "missing return statement                                  | without a return",
        "variable total might not have been initialized            | total is used before it has a value",
        "package system does not exist                             | System, not system",
        "reached end of file while parsing                         | closing curly brace",
    })
    void knownErrorsGetAPlainExplanation(String javac, String expectedFragment) {
        String explanation = CompilerHelp.explain(javac.replace("\\n", "\n")).orElseThrow();
        assertTrue(explanation.contains(expectedFragment), explanation);
    }

    @Test
    void lossyConversionIsNotExplainedAsAPlainTypeMismatch() {
        String explanation = CompilerHelp.explain("incompatible types: possible lossy conversion from double to int").orElseThrow();
        assertTrue(explanation.startsWith("Turning a double"), explanation);
    }

    @Test
    void unknownErrorsGetNoExplanation() {
        assertEquals(Optional.empty(), CompilerHelp.explain("something javac says rarely"));
    }
}
