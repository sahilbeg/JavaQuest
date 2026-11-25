# INC-11: A checkout step nobody can reach

The gift step (gift wrap ₹30, a name stitched on ₹50) is written, but checkout jumps from the address straight to UPI. Fix `src/checkout.ts`. `src/Checkout.tsx` draws each step and doesn't need changes.

The steps go `cart` → `address` → `gift` → `upi` → `done`, with the titles `Your cart`, `Delivery address`, `Make it a gift`, `Pay by UPI` and `Order placed`.

1. **Bronze:** Continue from the address reaches the gift step, headed `Make it a gift`. Continue from there goes on to UPI.
2. **Silver:** Back goes `upi` → `gift` → `address` → `cart`. Gift wrap plus a stitched name on a ₹450 Golu make the UPI step say `Pay ₹530.00 to dhaaga@upi`.
3. **Gold:** every step has its own title. `next` on `done`, and `back` on `cart` or `done`, change nothing. An unknown action throws `Unknown action: <type>`. A stitched name of only spaces costs nothing.

Tip: a `Record<Step, Step | null>` for the next and previous steps makes TypeScript list every step for you.

`src/main.tsx` shows checkout for one Golu in the preview.
