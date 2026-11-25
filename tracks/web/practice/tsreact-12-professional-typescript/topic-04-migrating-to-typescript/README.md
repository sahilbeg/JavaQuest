# Moving a JavaScript file to TypeScript

`giftOrder.js` was the last JavaScript file in the store. Step 1 of the move is done: it's now `src/giftOrder.ts`, and its three shapes are typed `any`, so it compiles exactly as the JavaScript did. Finish the move: give it real types, then fix every bug the types show you.

Gift wrap costs ₹30 (3000 paise) and a stitched name ₹50 (5000 paise), **for each item**.

1. **TODO 1:** replace the three `any` types with real object types:
   - `GiftOptions`: `giftWrap` (true or false) and `stitchedName` (text; `""` means no name).
   - `GiftLine`: `id`, `name`, `paise`, `qty` and `gift` (a `GiftOptions`).
   - `GiftOrder`: `id` and `lines` (a list of `GiftLine`).

   As soon as the types are real, the editor underlines the bugs in the three functions.
2. **TODO 2:** `extrasPaise(gift)` returns 0, 3000, 5000 or 8000. A name of only spaces is no name.
3. **TODO 3:** `lineTotal(line)` is `(paise + extras) × qty`.
4. **TODO 4:** `orderSummary(order)` returns text like `"3 items, ₹1,180.00"`: the number of items (every `qty` added up), then the sum of every `lineTotal`, formatted with `formatRupees`. One item reads `"1 item, ₹120.00"`.

The checks also read `giftOrder.ts` and fail while the code (not the comments) still says `any`.
