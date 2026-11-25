# Fetching data in server components

Make the catalogue page fetch its data on the server, so it arrives with the items already in it.

The Dhaaga API has two addresses:

- `https://api.dhaaga.in/items` answers a list like `[{ "id": "DC-1042", "name": "Golu the elephant", "paise": 45000 }]`.
- `https://api.dhaaga.in/stock` answers counts like `{ "DC-1042": 7, "DC-2001": 0 }`.

Change `src/lib/api.js`:

1. **TODO 1:** `getItems()` fetches the items address and returns its JSON. If the answer is not ok, it throws `new Error("Items API answered 503")` (with the real status).
2. **TODO 2:** `getStock()` does the same for the stock address, with the message `Stock API answered 500` (with the real status).

Change `src/app/items/page.jsx`:

3. **TODO 3:** `ItemsPage` is an `async` server component. Get the items and the stock **at the same time**, with `Promise.all`.
4. **TODO 4:** under the `<h1>Catalogue</h1>`, show one `<li>` per item, like `Golu the elephant: ₹450.00 (7 left)`. An item with 0 left (or missing from the stock) says `(Sold out)`. When there are no items, show `<p>No items yet. Check back soon!</p>` instead of the list.

`rupees()` in `src/lib/money.js` is done: `rupees(45000)` gives `₹450.00`. The checks use a pretend `fetch`, so no real network is used.
