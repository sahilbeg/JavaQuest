package jq.m06.mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnlockServiceTest {

    @Mock PaymentGateway gateway;
    @Mock BikeLock lock;
    @Mock Receipts receipts;

    UnlockService service;

    @BeforeEach
    void setUp() {
        service = new UnlockService(gateway, lock, receipts);
    }

    @Test
    @DisplayName("A good card and a working lock: charge 100 cents first, then open the lock")
    void successChargesThenOpens() {
        when(gateway.charge("rider-7", 100)).thenReturn("pay-1");
        when(lock.open("B-1042")).thenReturn(true);

        String result = service.unlock("rider-7", "B-1042");

        assertEquals("unlocked", result, "unlock should return \"unlocked\" when the charge and the lock both work");
        InOrder order = inOrder(gateway, lock);
        order.verify(gateway).charge(eq("rider-7"), eq(100L));
        order.verify(lock).open("B-1042");
    }

    @Test
    @DisplayName("Each unlock charges the rider exactly once, for exactly 100 cents")
    void chargesOnceForOneHundredCents() {
        when(gateway.charge(anyString(), anyLong())).thenReturn("pay-2");
        when(lock.open(anyString())).thenReturn(true);

        service.unlock("rider-7", "B-1042");

        verify(gateway, times(1)).charge(anyString(), anyLong());
        verify(gateway).charge("rider-7", 100L);
    }

    @Test
    @DisplayName("A declined card returns declined and never opens the lock")
    void declineNeverOpensLock() {
        when(gateway.charge(anyString(), anyLong())).thenThrow(new PaymentDeclinedException("card declined"));

        String result = service.unlock("rider-7", "B-1042");

        assertEquals("declined", result, "unlock should return \"declined\" when charge throws PaymentDeclinedException");
        verify(lock, never()).open(anyString());
        verify(receipts, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("A stuck lock refunds the payment and sends no receipt")
    void lockFailureRefunds() {
        when(gateway.charge(anyString(), anyLong())).thenReturn("pay-3");
        when(lock.open("B-1042")).thenReturn(false);

        String result = service.unlock("rider-7", "B-1042");

        assertEquals("lock failed", result, "unlock should return \"lock failed\" when lock.open returns false");
        verify(gateway).refund("pay-3");
        verify(receipts, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("The receipt carries the rider id and the payment id from the charge")
    void receiptCarriesPaymentId() {
        when(gateway.charge(anyString(), anyLong())).thenReturn("pay-42");
        when(lock.open(anyString())).thenReturn(true);

        service.unlock("rider-7", "B-1042");

        ArgumentCaptor<String> paymentId = ArgumentCaptor.forClass(String.class);
        verify(receipts).send(eq("rider-7"), paymentId.capture());
        assertEquals("pay-42", paymentId.getValue(), "the receipt should carry the payment id that charge returned");
        verify(gateway, never()).refund(anyString());
    }
}
