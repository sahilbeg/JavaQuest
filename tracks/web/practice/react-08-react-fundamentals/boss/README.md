# INC-08: Removing one item removes another

On the first sale day, removing an item from the cart changes the quantity shown for a different item. Removing one of two coaster sets removes both. The code is in `Cart.jsx`.

- Each item looks like `{ id: "DC-1044", name: "Mint keychain", paise: 12000, qty: 3 }`.
- `CartRow` keeps the quantity in its own state, so the customer can change it with the dropdown. Keep it that way.
- Keep the labels: each dropdown is labelled `Quantity for <name>`, and each button `Remove <name>`.

The checks come in three tiers:

1. **Bronze:** after removing a row, the rows below it still show their own quantity and line total.
2. **Silver:** a quantity the customer picked survives when a row above it is removed.
3. **Gold:** with two items called "Coaster set" but different ids, Remove takes away only the clicked one, with no key warnings in the console.

Try it in the preview first: change a quantity, remove the row above it, and watch. Press **Submit** to run the checks.
