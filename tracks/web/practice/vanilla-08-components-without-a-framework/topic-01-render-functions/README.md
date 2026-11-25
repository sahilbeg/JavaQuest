# Render functions: building elements from data

Write a small `h` helper in `src/h.js`, then build the product card with it in `src/cards.js`.

`h(tag, props, ...children)` returns one new element, not yet on the page.

1. **TODO 1:** skip a prop whose value is `null`, `undefined` or `false`. The prop `class` sets `className`.
2. **TODO 2:** the prop `dataset` is an object copied into `data-` attributes. A prop whose name starts with `on` and whose value is a function becomes a listener: `onClick` listens for `click`.
3. **TODO 3:** every other prop becomes an attribute with `setAttribute`. The value `true` sets an empty attribute, so `{ disabled: true }` disables a button.
4. **TODO 4:** append the children in order. Flatten arrays (at any depth), skip `null`, `undefined` and `false`, append elements as they are, and add anything else (text, numbers) as plain text, never as HTML.

In `src/cards.js`, a product looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 }`:

5. **TODO 5:** `productCard(product, onAdd)` returns this card, built with `h`. The price comes from `rupees(paise)` in `money.js`. Pressing the button calls `onAdd(product.id)`. When `stock` is 0, the `<li>` has the classes `product sold-out`, and the button is disabled and says `Sold out`.

   ```html
   <li class="product" data-id="DC-1042">
     <h3>Golu the elephant</h3>
     <p class="price">₹450.00</p>
     <button class="add" type="button">Add to cart</button>
   </li>
   ```

6. **TODO 6:** `productList(products, onAdd)` returns a `<ul class="products">` holding one `productCard` per product, in order. With no products, it holds one `<li class="empty">No items match.</li>` instead.

Open the preview to see the catalogue drawn by your components.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
