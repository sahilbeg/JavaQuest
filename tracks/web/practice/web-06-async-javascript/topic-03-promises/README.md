# Promises

Make checkout's UPI step handle every outcome. Change `src/payment.js`. Use promises with `then`, `catch` and `finally` here; `async` and `await` come in the next ticket.

1. **TODO 1:** `wait(ms)` returns a new `Promise` that is fulfilled after `ms` milliseconds. Use `setTimeout` inside it.
2. **TODO 2 and 3:** `paymentMessage(payment)` gets the bank's payment promise and returns a promise of the text to show:
   - fulfilled with `{ status: "SUCCESS", ref: "UPI-5521" }`: `Payment received, ref UPI-5521` (use the real `ref`)
   - fulfilled with any other status: `Payment pending`
   - rejected: `Payment failed. Please try again.` Your promise must not reject.
3. **TODO 4:** `closeQrWhenSettled(payment, closeQr)` calls `closeQr()` once the payment settles, whether it worked or failed. The promise it returns has the same outcome as `payment`: the same value, or the same error.

Press **Submit** in Cadence, or save the file in your editor. The checks use pretend timers and pretend bank answers.
