# Progressive enhancement: works without JavaScript

On a slow train, the shop page arrived but its script didn't, and Search and Add to cart did nothing. Make the page work with plain HTML first (`src/index.html`), then let the script make it nicer (`src/enhance.js`).

## The HTML layer (`src/index.html`)

1. **TODO 1:** the search becomes a real form: `<form id="search-form" action="/search" method="get" role="search">`, around the existing label, the input (add `name="q"`) and a `<button type="submit">Search</button>`.
2. **TODO 2:** in each of the four product cards:
   - the name in the `<h2>` becomes a link to its own page: `<a href="/products/DC-1042">Golu the elephant</a>`;
   - the button becomes a form: `<form class="add" action="/cart" method="post">` holding `<input type="hidden" name="id" value="DC-1042">` and `<button type="submit">Add to cart</button>`. Use each card's own id. Keep `class="product"` and `data-name` on every `<li>`.

## The JavaScript layer (`src/enhance.js`)

`enhance(root, fetchFn)` does nothing when `fetchFn` isn't a function, so the forms keep working the old way. That part is done.

3. **TODO 3:** when the search form is submitted: call `event.preventDefault()`, then hide (`hidden = true`) every `.product` whose `data-name` doesn't contain the trimmed, lower-case search words, and show the rest. Put `matchMessage(count)` in `#status` (`"No products match"`, `"1 product matches"`, `"3 products match"`). Then keep the search in the address: ``history.replaceState(null, "", `?q=${encodeURIComponent(text)}`)``, where `text` is the trimmed search.
4. **TODO 4:** when a `form.add` is submitted (the listener is ready):
   - call `event.preventDefault()` first, before any `await`, and disable the form's button;
   - send `fetchFn(form.getAttribute("action"), { method: "POST", body: new FormData(form) })`;
   - when `response.ok`, read `{ count }` from `await response.json()`, put it in `#cart-count`, and set `#status` to `Added Golu the elephant to your cart.` (the card's `data-name`);
   - when the answer isn't ok, or `fetchFn` throws, set `#status` to `Sorry, that didn't work. Please try again.`;
   - in `finally`, enable the button again.

The checks read your HTML as a browser would, and test the script with a pretend server. In the preview, a pretend server answers too: search, and add a few things.
