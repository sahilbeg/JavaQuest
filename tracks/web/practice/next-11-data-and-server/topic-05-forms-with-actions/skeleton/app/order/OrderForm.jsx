"use client";

import { useActionState } from "react";
import { ITEMS } from "../../lib/catalogue.js";

export const START = { errors: {}, values: { name: "", phone: "", itemId: "DC-1042", qty: "1" } };

/** The order form. action is the server action, placeOrder(prevState, formData). */
export function OrderForm({ action }) {
  // TODO 4: const [state, formAction, isPending] = useActionState(action, START);
  // and use formAction as the form's action instead of action.
  const state = START;
  const isPending = false;

  // TODO 5: under each field, show state.errors.<field> in a <p role="alert"> when there is one.
  // TODO 6: while isPending, disable the button and make it say "Placing order...".
  return (
    <form action={action} noValidate>
      <label htmlFor="name">Name</label>
      <input id="name" name="name" defaultValue={state.values.name} />

      <label htmlFor="phone">Phone</label>
      <input id="phone" name="phone" defaultValue={state.values.phone} />

      <label htmlFor="itemId">Item</label>
      <select id="itemId" name="itemId" defaultValue={state.values.itemId}>
        {ITEMS.map((item) => (
          <option key={item.id} value={item.id}>
            {item.name}
          </option>
        ))}
      </select>

      <label htmlFor="qty">Quantity</label>
      <input id="qty" name="qty" type="number" defaultValue={state.values.qty} />

      <button type="submit">Place order</button>
    </form>
  );
}
