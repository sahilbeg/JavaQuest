# FEAT-08: The catalogue as web components

Build the catalogue from pieces any page can use. A product is `{ id, name, paise, stock }`, a cart line is `{ id, qty }`, and the store's state is `{ products, lines }`. `src/store.js` (`createStore`) and `src/money.js` (`rupees`) are ready.

**`src/product-card.js`: `<product-card>`**

1. **TODO 1:** attach an open shadow root in the constructor, holding `CARD_HTML` (already written).
2. **TODO 2:** setting `card.product = product` stores it and draws it in the shadow root: `.name`, `.price` (from `rupees`), `.stock` (from `stockText`), and the `.add` button disabled when `stock` is 0. Setting it again draws again, and reading `card.product` gives the product back.
3. **TODO 3:** `stockText(stock)` returns `Sold out` for 0, `Only 4 left` (with the real number) for 1 to 5, and `In stock` above 5.
4. **TODO 4:** pressing `.add` dispatches `add-to-cart` from the card, with `detail: { id }`, `bubbles: true` and `composed: true`.

**`src/cart-count.js`: `<cart-count>`**

5. **TODO 5:** `countItems(lines)` returns the sum of every `qty`.
6. **TODO 6:** with its `store` property set before it joins the page, it shows `Cart (3)` straight away, follows every change, and unsubscribes when it leaves the page.

**`src/catalogue.js`**

7. **TODO 7:** `addLine(lines, id)` returns a **new** array: that line with `qty` one higher, or a new `{ id, qty: 1 }` at the end. The old array and its lines are never changed.
8. **TODO 8:** `createCatalogue(root, store)` fills `root` with one `<product-card>` per product, in order, each with its `data-id` and its `product` set. Whenever the store's products change, it keeps the card of every id that stays (setting its new `product`), makes cards for new ids, removes the cards of ids that are gone, and follows the new order.
9. **TODO 9:** it adds **one** `add-to-cart` listener on `root`, which adds that id to the store's `lines` with `addLine`.

Open the preview: add items, then press "Refresh stock" and watch the same cards update.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
