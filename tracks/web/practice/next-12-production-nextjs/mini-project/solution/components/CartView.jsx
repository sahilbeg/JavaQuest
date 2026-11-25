"use client";

import { useActionState } from "react";
import Link from "next/link";
import { placeOrder } from "../app/actions.js";
import { orderTotals } from "../lib/pricing.js";
import { formatRupees } from "../lib/money.js";

// The cart and checkout form. lines look like [{ id, name, paise, qty }].
export function CartView({ lines }) {
  const [state, formAction, pending] = useActionState(placeOrder, { error: "" });
  if (lines.length === 0) {
    return (
      <section>
        <h1>Your cart</h1>
        <p>Your cart is empty.</p>
        <Link href="/">Back to the shop</Link>
      </section>
    );
  }
  const totals = orderTotals(lines);
  return (
    <section>
      <h1>Your cart</h1>
      <ul>
        {lines.map((line) => (
          <li key={line.id}>
            {line.name} × {line.qty}: {formatRupees(line.paise * line.qty)}
          </li>
        ))}
      </ul>
      <p>Subtotal: {formatRupees(totals.subtotal)}</p>
      {totals.offer > 0 && <p>Diwali offer: {formatRupees(totals.offer)} off</p>}
      <p>Delivery: {totals.delivery === 0 ? "Free" : formatRupees(totals.delivery)}</p>
      <p>Total: {formatRupees(totals.total)}</p>
      <form action={formAction}>
        {lines.map((line) => (
          <input key={line.id} type="hidden" name="line" value={`${line.id}:${line.qty}`} />
        ))}
        <label htmlFor="name">Your name</label>
        <input id="name" name="name" autoComplete="name" required />
        <label htmlFor="phone">Phone number</label>
        <input id="phone" name="phone" type="tel" autoComplete="tel" required />
        {state.error && <p role="alert">{state.error}</p>}
        <button type="submit" disabled={pending}>
          Place order
        </button>
      </form>
    </section>
  );
}
