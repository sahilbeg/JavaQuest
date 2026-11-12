package jq.m01.stringbuilderformatting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReceiptFormatterTest {

    @Test
    @DisplayName("money shows a dollar sign and exactly 2 decimals")
    void moneyTwoDecimals() {
        assertEquals("$2.50", ReceiptFormatter.money(2.4999999),
                "money(2.4999999) should be \"$2.50\": use String.format(\"$%.2f\", amount)");
        assertEquals("$1.00", ReceiptFormatter.money(1.0),
                "money(1.0) should be \"$1.00\": always show 2 decimals");
    }

    @Test
    @DisplayName("line pads the label to 12 characters and right-aligns the money in 8")
    void lineLinesUp() {
        String line = ReceiptFormatter.line("Unlock fee", 1.0);
        assertEquals("Unlock fee     $1.00", line,
                "line(\"Unlock fee\", 1.0) should be \"Unlock fee     $1.00\": use String.format(\"%-12s%8s\", label, money(amount))");
        assertEquals(20, line.length(), "every line should be exactly 20 characters long: 12 for the label plus 8 for the money");
    }

    @Test
    @DisplayName("line keeps the money column straight for a longer price")
    void lineLongerPrice() {
        assertEquals("Ride 45 min   $13.50", ReceiptFormatter.line("Ride 45 min", 13.5),
                "line(\"Ride 45 min\", 13.5) should be \"Ride 45 min   $13.50\": the money ends at character 20");
    }

    @Test
    @DisplayName("receipt has one line per item and a TOTAL line, joined with \\n")
    void receiptJoinsLines() {
        String expected = "Unlock fee     $1.00\n"
                + "Ride 10 min    $1.50\n"
                + "TOTAL          $2.50";
        assertEquals(expected,
                ReceiptFormatter.receipt(new String[] {"Unlock fee", "Ride 10 min"}, new double[] {1.0, 1.5}),
                "receipt should append line(...) plus \"\\n\" for each item, then line(\"TOTAL\", 2.5) with no \\n after it");
    }

    @Test
    @DisplayName("receipt with no items still prints a TOTAL of $0.00")
    void receiptEmpty() {
        assertEquals("TOTAL          $0.00", ReceiptFormatter.receipt(new String[0], new double[0]),
                "receipt with empty arrays should be just the TOTAL line with $0.00");
    }
}
