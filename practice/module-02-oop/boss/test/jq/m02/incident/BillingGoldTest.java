package jq.m02.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingGoldTest {

    @Test
    @DisplayName("Gold: the invoice lists each ride and the total")
    void invoice() {
        Bike[] bikes = { new EBike("B-2001"), new CargoBike("B-3001") };
        String expected = """
                B-2001 ebike     10 min  $4.00
                B-3001 cargo     20 min  $6.00
                TOTAL                   $10.00""";
        assertEquals(expected, Billing.invoice(bikes, new int[] { 10, 20 }),
                "compare with the invoice layout in the KB page, character by character");
    }

    @Test
    @DisplayName("Gold: an empty fleet has only the total line")
    void emptyInvoice() {
        assertEquals("TOTAL                    $0.00", Billing.invoice(new Bike[0], new int[0]),
                "with no rides the invoice is just the TOTAL line");
    }
}
