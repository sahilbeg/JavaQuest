# INC-10: The cart shows undefined for a missing item

A retired item, `DC-1031`, makes the cart show `undefined × 2` and `₹NaN`. The cart is in `Cart.tsx`. It looks items up in `NAMES` and `PRICES` from `catalogue.ts`.

- A cart line looks like `{ id: "DC-1042", qty: 1 }`.
- A line for an item that is in the catalogue reads `Golu the elephant × 1` in `span.name`, with its line total in `span.line-total` and a button labelled `Remove Golu the elephant`.
- A line for an item that is **not** in the catalogue has the classes `cart-line missing`. Its `span.name` reads `DC-1031 is no longer available`, it has no line total, and its button is labelled `Remove DC-1031`.
- `p.count` shows `1 item` or `3 items`, counting only items that can be bought. Delivery is ₹60, free above ₹999 of subtotal.

The checks come in three tiers:

1. **Bronze:** a missing item never shows `undefined` or `NaN`, and the subtotal and total leave it out.
2. **Silver:** the missing line can be removed, and the count only counts items that can still be bought.
3. **Gold:** ids like `toString` and `constructor` are treated as missing, not as items.

The preview shows the broken cart. Press **Submit** to run the checks.
