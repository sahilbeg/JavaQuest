package jq.m06.solid;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentProcessorTest {

    static class ScooterCreditPayment implements PaymentMethod {
        @Override
        public String name() {
            return "scooter-credit";
        }

        @Override
        public String pay(long cents) {
            return "scooter-credit: charged " + cents + " cents";
        }
    }

    private static PaymentMethod create(Class<?> type) {
        assertTrue(PaymentMethod.class.isAssignableFrom(type),
                type.getSimpleName() + " should implement PaymentMethod: add implements PaymentMethod to its class line");
        try {
            return (PaymentMethod) type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            fail(type.getSimpleName() + " needs a public constructor with no parameters");
            return null;
        }
    }

    private static List<PaymentMethod> allThree() {
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add(create(CardPayment.class));
        methods.add(create(WalletPayment.class));
        methods.add(create(TransitPassPayment.class));
        return methods;
    }

    @Test
    @DisplayName("PaymentMethod is an interface and all three payment classes implement it")
    void shape() {
        assertTrue(PaymentMethod.class.isInterface(), "PaymentMethod should be an interface");
        for (Class<?> type : List.of(CardPayment.class, WalletPayment.class, TransitPassPayment.class)) {
            assertTrue(PaymentMethod.class.isAssignableFrom(type),
                    type.getSimpleName() + " should implement PaymentMethod");
        }
        assertDoesNotThrow(() -> PaymentProcessor.class.getConstructor(List.class),
                "PaymentProcessor should have a public constructor that takes a List<PaymentMethod>");
    }

    @Test
    @DisplayName("Each payment class has the right name and receipt text")
    void eachMethod() {
        PaymentMethod card = create(CardPayment.class);
        PaymentMethod wallet = create(WalletPayment.class);
        PaymentMethod transit = create(TransitPassPayment.class);
        assertEquals("card", card.name(), "CardPayment.name() should be \"card\"");
        assertEquals("wallet", wallet.name(), "WalletPayment.name() should be \"wallet\"");
        assertEquals("transit", transit.name(), "TransitPassPayment.name() should be \"transit\"");
        assertEquals("card: charged 250 cents", card.pay(250), "CardPayment.pay(250) text is wrong");
        assertEquals("wallet: charged 250 cents", wallet.pay(250), "WalletPayment.pay(250) text is wrong");
        assertEquals("transit: charged 250 cents", transit.pay(250), "TransitPassPayment.pay(250) text is wrong");
    }

    @Test
    @DisplayName("The processor routes each payment to the method with that name")
    void routes() {
        PaymentProcessor processor = new PaymentProcessor(allThree());
        assertEquals("card: charged 250 cents", processor.pay("card", 250),
                "pay(\"card\", 250) should go to CardPayment");
        assertEquals("wallet: charged 400 cents", processor.pay("wallet", 400),
                "pay(\"wallet\", 400) should go to WalletPayment");
        assertEquals("transit: charged 150 cents", assertDoesNotThrow(() -> processor.pay("transit", 150),
                "pay(\"transit\", 150) threw. The processor should look the name up in its list"),
                "pay(\"transit\", 150) should go to TransitPassPayment");
    }

    @Test
    @DisplayName("A brand-new payment method works without changing PaymentProcessor")
    void openForExtension() {
        PaymentProcessor processor = new PaymentProcessor(List.of(new ScooterCreditPayment()));
        String receipt = assertDoesNotThrow(() -> processor.pay("scooter-credit", 300),
                "the processor threw for a method it was given in its list. Route by name(), with no switch");
        assertEquals("scooter-credit: charged 300 cents", receipt,
                "pay should call the PaymentMethod from the constructor list whose name() matches");
    }

    @Test
    @DisplayName("An unknown payment method gets a clear IllegalArgumentException")
    void unknown() {
        PaymentProcessor processor = new PaymentProcessor(List.of(new ScooterCreditPayment()));
        Exception e = assertThrows(Exception.class, () -> processor.pay("cash", 250),
                "pay(\"cash\", 250) should throw, because no method is called cash");
        assertEquals(IllegalArgumentException.class, e.getClass(),
                "an unknown method should throw IllegalArgumentException");
        assertEquals("unknown payment method: cash", e.getMessage(),
                "the message should be exactly: unknown payment method: cash");
    }
}
