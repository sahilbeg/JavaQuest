# FEAT-09: Checkout with a UPI step

Build Dhaaga's checkout: cart, address, UPI payment, order placed. Money is in paise; show it with `rupees()` from `src/money.js`.

1. **TODO 1** in `src/totals.js`: `checkoutTotals(lines)` returns `{ items, delivery, total }`. Delivery is 6000 paise, free when `items` is above 99900. An empty cart has no delivery charge.
2. **TODO 2** in `src/address.js`: `validateAddress({ name, phone, pincode })` returns one message per bad field, as in ticket 7: `Please enter your name.`, `Phone needs 10 digits, like 98765 43210.`, `PIN code needs 6 digits.`
3. **TODO 3 to 7** in `src/checkoutReducer.js`: the steps go `cart` → `address` → `upi` → `done`.
   - `cartConfirmed` (on `cart`) goes to `address`.
   - `addressSaved` (on `address`) goes to `upi` and keeps `action.address`.
   - `wentBack` goes from `address` to `cart`, or from `upi` to `address`.
   - `paid` (on `upi`) goes to `done` and keeps `action.orderId`.
   - An action on the wrong step returns the state unchanged. An unknown type throws `Unknown action: <type>`.
4. **TODO 8 to 12** in `src/Checkout.jsx`: one screen per step.
   - **Cart:** heading `Your cart`; lines like `Coaster set × 2: ₹300.00`; `Items: ₹750.00`, `Delivery: ₹60.00` (or `Delivery: Free`), `To pay: ₹810.00`; a **Continue to address** button, disabled with the text `Your cart is empty.` when there are no lines.
   - **Address:** heading `Delivery address`; fields `Name`, `Phone`, `PIN code`; **Back** and **Continue to payment**. Messages show after the first try. A valid address is saved trimmed, with the phone's spaces removed. Going back to this step shows the saved address again.
   - **UPI:** heading `Pay by UPI`; `Pay ₹810.00 to dhaaga@upi`; `Delivering to Asha Rao, 411007`; **Back** and **I've paid**. I've paid calls `placeOrder({ lines, address, total })` once and moves on with the id it returns.
   - **Done:** heading `Order placed`; `Your order id is order-17.`
