# Drawing the page from data

Draw the catalogue from data, in `src/catalogue.js`. A product looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000, colour: "Indigo", stock: 4 }`. The data for the preview is in `src/data.js`.

`productCard(product)` returns one new card, not yet on the page:

```html
<li class="product" data-id="DC-1042">
  <h3>Golu the elephant</h3>
  <p class="price">₹450.00</p>
  <button class="add" type="button">Add to cart</button>
</li>
```

1. **TODO 1:** the `<li>`, its `<h3>` and its `.price`, with the price from `rupees(paise)` in `money.js`. All text goes in as plain text.
2. **TODO 2:** the `.add` button. When `stock` is 0, the card also gets the class `sold-out`, and the button is disabled and says `Sold out`.

`renderCatalogue(list, products)` draws the list:

3. **TODO 3:** the list ends up holding exactly one card per product, in order. Drawing again replaces the old cards; it never adds to them.
4. **TODO 4:** with no products, the list holds just `<li class="empty">No items match.</li>`.

5. **TODO 5:** `countText(count)` returns `No items` for 0, `1 item` for 1, and `5 items` for 5.

Open the preview and try the colour filter. Off-white matches nothing yet.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
