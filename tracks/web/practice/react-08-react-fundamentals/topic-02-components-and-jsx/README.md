# Components and JSX

Sana wants the top of the store as three components. Fill them in inside `StoreFront.jsx`. The constant `TAGLINE` is already there.

1. **TODO 1:** `StoreHeader` returns a `<header className="store-header">` holding, in this order:
   - an `<h1>` that says `Dhaaga & Co.`
   - a `<p>` that shows `TAGLINE` with curly braces
   - `<img src="golu.png" alt="Golu the crocheted elephant" />`
2. **TODO 2:** `OpeningHours` returns a `<ul className="hours">` with two `<li>` items, in this order:
   - `FC Road stall: Sat and Sun, 10:00 to 20:00`
   - `Aundh shop: Tue to Sun, 11:00 to 21:00`
3. **TODO 3:** `StoreFront` returns a `<main>` that shows `<StoreHeader />` and then `<OpeningHours />`.

The texts must match exactly. There is no `golu.png` file yet, so the preview shows the alt text where the photo goes.

`main.jsx` already draws `StoreFront` into the page, so the preview updates as you save. Press **Submit** to run the checks.
