# Forms with server actions

Make the order form on dhaaga.in explain its mistakes, keep what was typed, show when it's busy, and move to the new order's page when it works.

`src/lib/catalogue.js` and `src/lib/orders.js` are done. `findItem(id)` gives an item or `undefined`. `createOrder({ name, phone, itemId, qty })` saves an order and returns it with its id, like `order-20`. `src/app/order/page.jsx` is done too: it shows `<OrderForm action={placeOrder} />`.

Change `src/app/order/actions.js`. `placeOrder(prevState, formData)` is used with `useActionState`, so the state comes first:

1. **TODO 1:** read `name`, `phone`, `itemId` and `qty` from the form, as text, into a `values` object.
2. **TODO 2:** check them and collect messages in an `errors` object, keyed by field:
   - `name` empty (after trimming): `Please enter your name.`
   - `phone` without exactly 10 digits once spaces are removed: `Phone needs 10 digits, like 98765 43210.`
   - `itemId` not in the catalogue: `Please choose an item.`
   - `qty` not a whole number from 1 to 5: `Choose between 1 and 5.`

   If there are any errors, return `{ errors, values }` and save nothing.
3. **TODO 3:** otherwise call `createOrder` with the name trimmed, the phone without spaces, the item id and the quantity as a number. Then `redirect("/order/<id>")`, for example `/order/order-20`. Keep `redirect` outside any `try`.

Change `src/app/order/OrderForm.jsx`, a client component that gets the action as a prop:

4. **TODO 4:** `const [state, formAction, isPending] = useActionState(action, START);` and put `formAction` on the form.
5. **TODO 5:** under each field, show its message from `state.errors` in a `<p role="alert">`. Fill each field from `state.values` with `defaultValue`.
6. **TODO 6:** while `isPending`, the button is disabled and says `Placing order...`; otherwise it says `Place order`.

The checks call `placeOrder` with a `FormData` they build, use a pretend `redirect` that throws `NEXT_REDIRECT /order/order-20`, and give `OrderForm` a pretend action.
