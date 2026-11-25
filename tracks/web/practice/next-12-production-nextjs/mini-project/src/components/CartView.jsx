"use client";

import { useActionState } from "react";
import Link from "next/link";
import { placeOrder } from "../app/actions.js";
import { orderTotals } from "../lib/pricing.js";
import { formatRupees } from "../lib/money.js";

// The cart and checkout form. lines look like [{ id, name, paise, qty }].
export function CartView({ lines }) {
  const [state, formAction, pending] = useActionState(placeOrder, { error: "" });
  // TODO 9: with no lines, show "Your cart is empty." and a link "Back to the shop" to /, and no form.
  // Otherwise list each line as "Golu the elephant × 2: ₹900.00", then "Subtotal: ...", "Diwali offer: ₹179.90 off"
  // (only when there is an offer), "Delivery: ₹60.00" (or "Delivery: Free") and "Total: ...", using orderTotals.
  // Then a form with action={formAction}: a hidden input name="line" value="DC-1042:2" per line, labelled inputs
  // "Your name" (name="name") and "Phone number" (name="phone"), state.error in a role="alert" paragraph, and a
  // "Place order" submit button that is disabled while pending.
  return (
    <section>
      <h1>Your cart</h1>
    </section>
  );
}
