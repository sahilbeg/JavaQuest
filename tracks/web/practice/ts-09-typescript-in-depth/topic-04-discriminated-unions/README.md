# Discriminated unions

`Payment` in `src/payment.ts` is now a discriminated union: its `mode` says which fields a payment has. `rupees` and `assertNever` are done for you.

1. **TODO 1 and 2:** `receiptLine(payment)` returns the receipt line. Switch on `payment.mode`, and add a `default` case that returns `assertNever(payment)`, so an unknown mode throws.
   - CASH: `Cash ₹450.00, change ₹50.00`
   - UPI: `UPI ₹450.00, ref UPI-5521`
   - CARD: `Card ₹899.00, ending 4242`
2. **TODO 3:** `changePaise(payment)` returns `tenderedPaise - paise` for cash, and `0` for UPI and card.
3. **TODO 4:** `totalsByMode(payments)` returns the total paise for each mode: `{ CASH, UPI, CARD }`, with `0` for a mode nobody used.

`src/` is type-checked in strict mode before the checks run, so reading `upiRef` from a payment you haven't narrowed is a type error.
