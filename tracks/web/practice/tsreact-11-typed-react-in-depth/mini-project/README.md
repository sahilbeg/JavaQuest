# FEAT-11: Typed checkout with a UPI step

Build Dhaaga's checkout: cart, address, UPI payment, order placed. Money is in paise; show it with `rupees()` from `src/money.ts`.

1. **TODO 1** in `src/totals.ts`: `checkoutTotals(lines)` returns `{ items, delivery, total }`. Delivery is 6000 paise, free when `items` is above 99900. An empty cart has no delivery charge.
2. **TODO 2** in `src/address.ts`: `validateAddress(form)` returns one message per bad field:
   - `name` empty after trimming: `Please enter your name.`
   - `phone` not exactly 10 digits once spaces are removed: `Phone needs 10 digits, like 98765 43210.`
   - `pincode` not exactly 6 digits after trimming: `PIN code needs 6 digits.`
3. **TODO 3 to 7** in `src/checkoutMachine.ts`: the reducer moves between the state shapes:
   - `cartConfirmed` (on `cart`) gives `{ step: "address", saved: null }`.
   - `addressSaved` (on `address`) gives `{ step: "upi", address: action.address }`.
   - `wentBack` goes from `address` to `{ step: "cart" }`, or from `upi` to `{ step: "address", saved: <its address> }`.
   - `paid` (on `upi`) gives `{ step: "done", address, orderId: action.orderId }`.
   - An action on the wrong step returns the state unchanged (the same object). An unknown type throws `Unknown action: <type>`.
4. **TODO 8 to 10** in `src/Checkout.tsx`:
   - use `useReducer(checkoutReducer, START)`,
   - show `CartStep`, `AddressStep`, `UpiStep` or `DoneStep` for the state's step (they're done, in `src/steps.tsx`),
   - when the customer taps **I've paid**, call `placeOrder({ lines, address, total })` once (the saved, cleaned address, and `total` in paise, like `81000`), then dispatch `paid` with the id it returns.

`src/main.tsx` shows checkout for two coaster sets and Golu, with a pretend `placeOrder`.
