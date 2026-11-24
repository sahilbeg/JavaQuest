# Forms in React

Build the order form in `OrderForm.jsx`. The fields, labels, `ITEMS` and `GIFT_WRAP_PAISE` are already there. They just aren't connected to state yet.

1. **TODO 1:** Keep each field in state: `name` (`""`), `phone` (`""`), `code` (`"CST"`), `qty` (`1`, a number) and `giftWrap` (`false`).
2. **TODO 2:** Work out `totalPaise` from state: the chosen item's `paise` times `qty`, plus `GIFT_WRAP_PAISE` (₹30) when gift wrap is ticked. Two tote bags show `Total: ₹1,798.00`.
3. **TODO 3:** `handleSubmit` stops the page from reloading, then calls `onSubmit` with one object:
   `{ name, phone, code, qty, giftWrap }`, for example `{ name: "Asha Deshpande", phone: "98765 43210", code: "AMI", qty: 3, giftWrap: true }`.
4. **TODO 4:** Make every field controlled. Text boxes and selects use `value` and `event.target.value`. The checkbox uses `checked` and `event.target.checked`. Store `qty` as a number.
5. **TODO 5:** Disable Place order while the name or the phone is blank. A box holding only spaces counts as blank.

Keep the labels and ids as they are: the checks find each field by its label.

The preview shows the form and, after you send it, the order object it produced. Press **Submit** to run the checks.
