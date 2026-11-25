"use client";

import { useActionState } from "react";
import { ITEMS } from "../../lib/catalogue.js";

export const START = { errors: {}, values: { name: "", phone: "", itemId: "DC-1042", qty: "1" } };

/** The order form. action is the server action, placeOrder(prevState, formData). */
export function OrderForm({ action }) {
  const [state, formAction, isPending] = useActionState(action, START);
  const { errors, values } = state;

  return (
    <form action={formAction} noValidate>
      <label htmlFor="name">Name</label>
      <input id="name" name="name" defaultValue={values.name} />
      {errors.name && <p role="alert">{errors.name}</p>}

      <label htmlFor="phone">Phone</label>
      <input id="phone" name="phone" defaultValue={values.phone} />
      {errors.phone && <p role="alert">{errors.phone}</p>}

      <label htmlFor="itemId">Item</label>
      <select id="itemId" name="itemId" defaultValue={values.itemId}>
        {ITEMS.map((item) => (
          <option key={item.id} value={item.id}>
            {item.name}
          </option>
        ))}
      </select>
      {errors.itemId && <p role="alert">{errors.itemId}</p>}

      <label htmlFor="qty">Quantity</label>
      <input id="qty" name="qty" type="number" defaultValue={values.qty} />
      {errors.qty && <p role="alert">{errors.qty}</p>}

      <button type="submit" disabled={isPending}>
        {isPending ? "Placing order..." : "Place order"}
      </button>
    </form>
  );
}
