# Props: passing data down

Sana wants one `ProductCard` that works for every product. Fill in `ProductCard.jsx`. The `rupees` helper in `money.js` is finished: `rupees(45000)` gives `"₹450.00"`.

1. **TODO 1:** `ProductCard` unpacks `name`, `paise` and `colour` from its props. `colour` defaults to `"Off-white"`.
2. **TODO 2:** The card shows:
   - `name` in the `<h3>`
   - `rupees(paise)` in `<p className="price">`
   - `Colour: ` and then the colour in `<p className="colour">`, for example `Colour: Indigo`
3. **TODO 3:** `Badge` shows its `children` inside `<span className="badge">`.
4. **TODO 4:** `FeaturedProducts` shows three `ProductCard`s after the heading, in this order:

| name | paise | colour |
|---|---|---|
| Golu the elephant | 45000 | Indigo |
| Marigold coaster set | 15000 | Marigold |
| Indigo tote bag | 89900 | Indigo |

Pass the prices as numbers, in curly braces. The preview shows `FeaturedProducts`. Press **Submit** to run the checks.
