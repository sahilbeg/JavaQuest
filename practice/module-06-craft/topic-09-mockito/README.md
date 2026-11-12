# Mocks with Mockito

Finish `UnlockService.unlock` so a rider can unlock a bike without the tests charging real cards or opening real locks.

`UnlockService` gets three helpers in its constructor: a `PaymentGateway`, a `BikeLock` and `Receipts`. In the tests, all three are Mockito mocks.

1. **TODO 1:** charge the rider `UNLOCK_FEE_CENTS` (100 cents) with `gateway.charge` and keep the payment id it returns.
2. **TODO 2:** if `charge` throws `PaymentDeclinedException`, return `"declined"`. Do not touch the lock.
3. **TODO 3:** open the lock with `lock.open(bikeId)`. If it returns `false`, call `gateway.refund(paymentId)` and return `"lock failed"`.
4. **TODO 4:** send the receipt with `receipts.send(riderId, paymentId)`, then return `"unlocked"`.

## How to read the tests

Open `UnlockServiceTest.java`. Each test has three parts:

- `when(...).thenReturn(...)` or `thenThrow(...)` scripts what the card and the lock answer.
- `service.unlock("rider-7", "B-1042")` runs your code once.
- `assertEquals` checks the returned text. `verify` checks which calls your code made.

The tests check that you charge before you open the lock, never open the lock after a decline, refund on a stuck lock, never send a receipt after a failure, and put the right payment id on the receipt.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
