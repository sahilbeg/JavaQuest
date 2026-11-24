# INC-04: The Diwali discount is applied twice

Fix `applyDiwaliOffer(cart)` in `src/diwali.js`. The Diwali offer is 10% off orders above ₹1,500 (150000 paise), with the discount rounded down to whole paise.

A cart looks like `{ id: "order-17", lines: [{ code: "BAG", paise: 89900, qty: 2 }, { code: "CST", paise: 15000, qty: 1 }], totalPaise: 194800 }`. The function returns the cart with `discountPaise` (0 when the offer doesn't apply) and `totalPaise`, the amount the customer pays.

1. **Bronze:** apply the offer once per order, however many lines there are. The cart above gets `discountPaise` 19480 and `totalPaise` 175320. A cart of ₹1,500 or less gets `discountPaise` 0 and keeps its total.
2. **Silver:** never change the cart you were given. Return a new object with the same `id` and `lines`.
3. **Gold:** calling `applyDiwaliOffer` again on its own result gives the same result, even for a cart whose total drops below ₹1,500 after the discount.
