package jq.m00.incident;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

final class ScreenOutput {

    private ScreenOutput() {}

    static List<String> linesPrintedByMain() {
        PrintStream original = System.out;
        var captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true));
        try {
            KioskScreen.main(new String[0]);
        } finally {
            System.setOut(original);
        }
        return captured.toString().lines().toList();
    }
}
