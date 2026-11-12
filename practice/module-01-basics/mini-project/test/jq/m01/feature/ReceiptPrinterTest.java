package jq.m01.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReceiptPrinterTest {

    @Test
    @DisplayName("each bike type has its own price per minute")
    void prices() {
        assertEquals(0.15, ReceiptPrinter.pricePerMinute("classic"), 0.001, "classic bikes cost 0.15 per minute");
        assertEquals(0.30, ReceiptPrinter.pricePerMinute("ebike"), 0.001, "e-bikes cost 0.30 per minute");
        assertEquals(0.25, ReceiptPrinter.pricePerMinute("cargo"), 0.001, "cargo bikes cost 0.25 per minute");
        assertEquals(-1, ReceiptPrinter.pricePerMinute("tandem"), 0.001, "unknown types return -1");
    }

    @Test
    @DisplayName("non-members pay the unlock fee and members get 30 free minutes")
    void fares() {
        assertEquals(8.50, ReceiptPrinter.fare("ebike", 25, false), 0.001, "1.00 + 25 x 0.30 = 8.50");
        assertEquals(0.0, ReceiptPrinter.fare("classic", 20, true), 0.001, "a 20 minute member ride is free");
        assertEquals(3.00, ReceiptPrinter.fare("ebike", 40, true), 0.001, "a member pays 10 x 0.30 = 3.00 for 40 minutes");
    }

    @Test
    @DisplayName("the receipt matches the design line for line")
    void receipt() {
        String expected = """
                SPOKE RIVERTON
                Bike     B-1042
                From     Riverside
                Type     ebike
                Time     25 min
                Fare     $8.50
                Thank you for riding!""";
        assertEquals(expected, ReceiptPrinter.receipt("B-1042", "Riverside", "ebike", 25, false),
                "compare your receipt with the design in the KB page, line by line");
    }

    @Test
    @DisplayName("a member receipt shows the member fare")
    void memberReceipt() {
        String receipt = ReceiptPrinter.receipt("B-2001", "Old Mill", "cargo", 50, true);
        assertEquals("Fare     $5.00", receipt.lines().toList().get(5), "a 50 minute member cargo ride costs 20 x 0.25 = 5.00");
    }

    @Test
    @DisplayName("an unknown bike type gets a clear message")
    void unknownType() {
        assertEquals("Unknown bike type", ReceiptPrinter.receipt("B-1", "Harbor", "tandem", 10, false),
                "unknown types should return exactly: Unknown bike type");
    }
}
