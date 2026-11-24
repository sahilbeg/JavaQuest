# INC-01: A blind customer can't place an order

The order page in `index.html` looks fine, but a screen reader can't use it. Fix the HTML. Keep every field's `id`, `name` and checks as they are.

1. **Bronze:** give every field in the form, the three inputs and the select, a `<label>` joined with `for` and `id`: `Your name`, `Phone number`, `Item` and `How many?`. Placeholders don't count as labels.
2. **Silver:** replace the clickable `div` with `<button type="submit">Place order</button>` inside the form. Give each product photo alt text that describes it, and give the stitched line `alt=""`.
3. **Gold:** make `Place an order` the page's one `h1`, and `Your details` an `h2`. Turn the outer divs into a `header` (with the links in a `nav`), one `main` holding the `h1` and the form, and a `footer`.

Bronze resolves the incident. Silver and gold earn bonus XP.
