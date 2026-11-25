# Typed forms

Rebuild the custom-order form in `CustomOrderForm.tsx` around one typed object, `OrderRequest`.

1. **TODO 1:** Finish `update(key, value)`: copy the form with that one field changed.
2. **TODO 2:** The **Item** select: check `e.target.value` with `isItemCode`, then `update("item", ...)`.
3. **TODO 3:** The **Quantity** select: store a number, not text.
4. **TODO 4:** The **Gift wrap (₹30)** checkbox: store whether it is ticked.
5. **TODO 5:** `p.total` shows `Total: ₹1,798.00`: the item's price times the quantity, plus ₹30 (`GIFT_WRAP_PAISE`) with gift wrap.
6. **TODO 6:** `handleSubmit`: with an empty name (spaces don't count), show `Please enter your name` in `p.error` and send nothing. Otherwise clear the error and call `onSend(form)` once.

The name box is already wired to `update("name", ...)`, so it works as soon as TODO 1 does. Prices are in `items.ts`. Keep the labels: the checks find the fields by them. Press **Submit** to run the checks.
