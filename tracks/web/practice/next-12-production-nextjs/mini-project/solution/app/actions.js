"use server";

import { redirect } from "next/navigation";
import { getItem, createOrder } from "../lib/store.js";
import { orderTotals } from "../lib/pricing.js";

// The checkout form sends a "line" field per cart line ("DC-1042:2"), plus name and phone.
// Used with useActionState, so it gets (prevState, formData) and returns the next state, { error }.
export async function placeOrder(prevState, formData) {
  const entries = formData.getAll("line").map(String);
  if (entries.length === 0) {
    return { error: "Your cart is empty" };
  }
  const name = String(formData.get("name") ?? "").trim();
  if (name === "") {
    return { error: "Please enter your name" };
  }
  const phone = String(formData.get("phone") ?? "").replace(/\s/g, "");
  if (!/^\d{10}$/.test(phone)) {
    return { error: "Please enter a 10-digit phone number" };
  }
  const lines = [];
  for (const entry of entries) {
    const [id, qtyText = ""] = entry.split(":");
    const item = await getItem(id);
    const qty = Number(qtyText);
    if (!item || !/^\d+$/.test(qtyText) || qty < 1 || qty > 5) {
      return { error: "Something in your cart can't be sold. Please check it." };
    }
    lines.push({ id: item.id, qty, paise: item.paise });
  }
  const order = await createOrder({ name, phone, lines, totals: orderTotals(lines) });
  if (!order) {
    return { error: "Some items just sold out. Please check your cart." };
  }
  redirect(`/orders/${order.id}`);
}
