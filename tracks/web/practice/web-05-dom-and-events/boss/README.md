# INC-05: The cart empties on refresh

The cart in `src/cart.js` should survive a page reload, and it doesn't. Fix it without changing how the cart looks.

1. **Bronze:** every change to the cart is saved under `dhaaga:cart` straight away, and the saved value includes the item that was just added.
2. **Silver:** on start, the cart loads what was saved under `dhaaga:cart`. With nothing saved, the empty cart shows. With broken text saved (like `{oops`), the empty cart shows instead of a crash, and the next add saves a good cart again.
3. **Gold:** the cart is saved as whole lines, like `[{"id":"DC-1042","qty":2}]`, so two Golus come back as two and the total counts them. A saved value that isn't a list gives an empty cart. From a list, keep only lines that are objects with a string `id` that is in the catalogue and a `qty` that is a whole number above 0.

The checks open the page, click Add buttons, then "refresh" by drawing a fresh page with `createCart` while localStorage keeps what was saved.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
