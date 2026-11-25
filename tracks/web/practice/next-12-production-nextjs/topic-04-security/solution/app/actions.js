"use server";

import { getItem } from "../lib/catalogue.js";
import { addOrder } from "../lib/orders.js";

// The Buy now form sends id, qty and, from an old version of the page, a hidden paise field.
// Anyone can call this action with any fields they like, not only our form.
export async function placeOrder(formData) {
  const item = await getItem(String(formData.get("id")));
  if (!item) {
    return { ok: false, error: "Unknown item" };
  }
  const qty = Number(formData.get("qty"));
  if (!Number.isInteger(qty) || qty < 1 || qty > 5) {
    return { ok: false, error: "Pick a quantity from 1 to 5" };
  }
  const order = addOrder({ itemId: item.id, qty, totalPaise: item.paise * qty });
  return { ok: true, orderId: order.id, totalPaise: order.totalPaise };
}
