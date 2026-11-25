# Lifting typed state

The header, the item list and the cart box disagree. Make the cart one source of truth.

**`ShopPage.tsx`** is the parent of all three.

1. **TODO 1:** Keep the cart here, in state: `useState<CartLine[]>([])`.
2. **TODO 2:** `add(item)`: if the item is already in the cart, raise that line's `qty` by 1; otherwise add a new line `{ item, qty: 1 }` at the end. Always make a new array.
3. **TODO 3:** `remove(id)`: drop the line whose item has that id.
4. **TODO 4:** Pass `onRemove={remove}` to `CartSummary`.

**`CartSummary.tsx`**

5. **TODO 5:** Add `onRemove: (id: string) => void` to `CartSummaryProps`, and a button in each line, labelled `Remove <name>` (for example `aria-label="Remove Golu the elephant"`), that calls `onRemove` with the item's id.

`ItemList.tsx` is finished: it calls `onAdd(item)`. The header shows `Cart (3)` with the total quantity. Each cart line reads `Golu the elephant × 2`, and `p.cart-total` shows `Total: ₹1,050.00`. Press **Submit** to run the checks.
