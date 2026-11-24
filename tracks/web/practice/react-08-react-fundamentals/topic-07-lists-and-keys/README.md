# Lists and keys

Draw the catalogue from data instead of by hand. Fill in `Catalogue.jsx`. Each product looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000 }`.

1. **TODO 1:** When `products` is empty, `ProductList` returns `<p className="empty">Nothing here yet.</p>` instead of the list.
2. **TODO 2:** Otherwise it fills the `<ul className="product-list">` with one `<li>` per product, using `map`. Each `<li>`:
   - has `key={p.id}`
   - holds `<span className="name">` with the name, then `<span className="price">` with `rupees(p.paise)`
3. **TODO 3:** `GiftsUnder` keeps only the products whose `paise` is below `maxPaise`. An item that costs exactly the limit is left out.
4. **TODO 4:** It shows them cheapest first, without changing the order of the `products` array it was given.

One check watches the console: if React warns about keys, it fails.

The preview shows the whole catalogue and the "Gifts under ₹500.00" shelf. Press **Submit** to run the checks.
