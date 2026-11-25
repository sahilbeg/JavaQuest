"use server";

import { redirect } from "next/navigation";
import { createOrder } from "../../lib/orders.js";

// requestId -> the promise of its order. Stored before any await, so a second send finds the first one's work.
const placing = new Map();

/** The checkout form's action. The form sends name, phone, itemId, qty and a hidden requestId. */
export async function placeOrder(prevState, formData) {
  const requestId = String(formData.get("requestId") ?? "");
  const details = {
    name: String(formData.get("name") ?? "").trim(),
    phone: String(formData.get("phone") ?? "").replace(/\s/g, ""),
    lines: [{ itemId: String(formData.get("itemId") ?? ""), qty: Number(formData.get("qty") ?? 1) }],
  };

  let pending = requestId ? placing.get(requestId) : undefined;
  if (!pending) {
    pending = createOrder(details);
    if (requestId) placing.set(requestId, pending);
  }
  const order = await pending;
  redirect(`/orders/${order.id}`);
}
