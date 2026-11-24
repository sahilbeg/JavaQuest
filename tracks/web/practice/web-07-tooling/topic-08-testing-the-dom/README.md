# WEB-0708: The sold-out button keeps coming back

Rebuild the product card so it passes the checks in `test/productCard.test.js`. Read that file first: it shows how a page is tested with `beforeEach`, `querySelector`, `click()` and `vi.fn()`.

Change only `src/productCard.js`. `src/money.js` is finished: `formatRupees(45000)` gives `"₹450.00"`.

## stockText(stock)

- `0` gives `"Sold out"`.
- `1` to `3` gives `"Only 1 left"`, `"Only 2 left"`, `"Only 3 left"`.
- More than 3 gives `"In stock"`.

## renderProduct(container, product, onAdd)

`product` looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 2 }`. Draw this into `container`, replacing whatever was there:

```html
<article class="product" data-id="DC-1042">
  <h3>Golu the elephant</h3>
  <p class="price">₹450.00</p>
  <p class="stock">Only 2 left</p>
  <button type="button">Add to cart</button>
</article>
```

- Build it with `document.createElement` and set every text with `textContent`, never `innerHTML`.
- When `stock` is `0`, the button is disabled.
- Clicking the button calls `onAdd(product.id)` once. A disabled button never calls it.
- Return the `article` element.
