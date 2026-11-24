# Finding elements: querySelector

Fix the counter's price checker in `src/find.js`. Every function gets a `root` to search in: `document`, or one element.

1. **TODO 1:** `productCards(root)` returns every `.product` card as a real array.
2. **TODO 2:** `productTitles(root)` returns the trimmed `<h3>` text of every product, in page order. The cart's `<h3>` is not a product.
3. **TODO 3:** `findProduct(root, id)` returns the `.product` whose `data-id` is `id`, or `null`.
4. **TODO 4:** `priceOf(root, id)` returns the trimmed text of that product's own `.price`, like `"₹120.00"`, or `""` when there is no such product. The header has a `.price` note too: don't read it.
5. **TODO 5:** `soldOutTitles(root)` returns the `<h3>` text of every product that also has the class `sold-out`.

Open the preview: the line at the bottom shows the keychain's price and what is sold out.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
