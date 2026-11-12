package jq.m06.refactoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvoiceGeneratorTest {

    private static final List<String[]> THREE_RIDES = List.of(
            new String[] {"B-1042", "classic", "12"},
            new String[] {"B-2001", "ebike", "45"},
            new String[] {"B-3003", "cargo", "5"});

    @Test
    @DisplayName("Golden master: a BASIC invoice comes out exactly as before")
    void goldenMasterBasic() {
        String expected = "Spoke invoice\n"
                + "Membership: BASIC\n"
                + "B-1042 | classic | 12 min | $2.80\n"
                + "B-2001 | ebike | 45 min | $14.50\n"
                + "B-3003 | cargo | 5 min | $2.25\n"
                + "Rides: 3\n"
                + "Total: $19.55\n";
        assertEquals(expected, InvoiceGenerator.generate(THREE_RIDES, "BASIC"),
                "The BASIC invoice changed. A refactoring must keep every character of the output the same");
    }

    @Test
    @DisplayName("Golden master: PLUS and PRO invoices come out exactly as before")
    void goldenMasterMembers() {
        String plus = "Spoke invoice\n"
                + "Membership: PLUS\n"
                + "B-1042 | classic | 12 min | $0.00\n"
                + "B-2001 | ebike | 45 min | $4.50\n"
                + "B-3003 | cargo | 5 min | $0.00\n"
                + "Rides: 3\n"
                + "Total: $4.50\n";
        assertEquals(plus, InvoiceGenerator.generate(THREE_RIDES, "PLUS"),
                "The PLUS invoice changed. PLUS gets 30 free minutes and no unlock fee, exactly as before");

        List<String[]> proRides = List.of(
                new String[] {"B-2001", "ebike", "45"},
                new String[] {"B-4004", "cargo", "70"});
        String pro = "Spoke invoice\n"
                + "Membership: PRO\n"
                + "B-2001 | ebike | 45 min | $0.00\n"
                + "B-4004 | cargo | 70 min | $2.50\n"
                + "Rides: 2\n"
                + "Total: $2.50\n";
        assertEquals(pro, InvoiceGenerator.generate(proRides, "PRO"),
                "The PRO invoice changed. PRO gets 60 free minutes and no unlock fee, exactly as before");
    }

    @Test
    @DisplayName("Golden master: an empty month and an unknown bike type behave as before")
    void goldenMasterEdges() {
        assertEquals("Spoke invoice\nMembership: PRO\nNo rides this month.\nTotal: $0.00\n",
                InvoiceGenerator.generate(List.of(), "PRO"),
                "An invoice with no rides changed. It should still say No rides this month. and Total: $0.00");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> InvoiceGenerator.generate(List.<String[]>of(new String[] {"B-5005", "scooter", "3"}), "BASIC"),
                "An unknown bike type should still throw IllegalArgumentException");
        assertEquals("Unknown bike type: scooter", error.getMessage(),
                "The error message for an unknown bike type should not change");
    }

    @Test
    @DisplayName("InvoiceLine is a record")
    void invoiceLineIsRecord() {
        assertTrue(InvoiceLine.class.isRecord(),
                "InvoiceLine should be a record: public record InvoiceLine(String bikeId, String type, int minutes, long cents)");
    }

    @Test
    @DisplayName("lines() turns each ride into an InvoiceLine with the right cents")
    void linesWorkOutCents() {
        List<InvoiceLine> lines = InvoiceGenerator.lines(THREE_RIDES, "BASIC");
        assertEquals(3, lines.size(), "lines() should return one InvoiceLine for each of the 3 rides");
        InvoiceLine first = lines.get(0);
        assertEquals("B-1042", first.bikeId(), "The first line should keep the bike id B-1042");
        assertEquals("classic", first.type(), "The first line should keep the type classic");
        assertEquals(12, first.minutes(), "The first line should have 12 minutes as a number");
        assertEquals(280, first.cents(), "A 12 minute BASIC classic ride is 280 cents: 100 unlock plus 12 at 15");
        assertEquals(1450, lines.get(1).cents(), "A 45 minute BASIC e-bike ride is 1450 cents: 100 plus 45 at 30");
        assertEquals(450, InvoiceGenerator.lines(THREE_RIDES, "PLUS").get(1).cents(),
                "A 45 minute PLUS e-bike ride is 450 cents: 15 paid minutes at 30, no unlock fee");
    }

    @Test
    @DisplayName("format() prints one ride line exactly like the invoice does")
    void formatOneLine() {
        assertEquals("B-1042 | classic | 12 min | $2.80",
                InvoiceGenerator.format(new InvoiceLine("B-1042", "classic", 12, 280)),
                "format() should give bike id, type, minutes and money separated by \" | \", with no newline");
        assertEquals("B-3003 | cargo | 5 min | $0.05",
                InvoiceGenerator.format(new InvoiceLine("B-3003", "cargo", 5, 5)),
                "format() should pad cents to two digits, so 5 cents prints as $0.05");
    }
}
