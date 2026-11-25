"use server";

import { getItem } from "../lib/catalogue.js";
import { addOrder } from "../lib/orders.js";

// The Buy now form sends id, qty and, from an old version of the page, a hidden paise field.
// Anyone can call this action with any fields they like, not only our form.
export async function placeOrder(formData) {
  const id = String(formData.get("id"));
  const qty = Number(formData.get("qty"));
  // TODO 1: never trust a price from the form. Look the item up with getItem(id) and use the server's item.paise.
  const paise = Number(formData.get("paise"));
  // TODO 2: an unknown item returns { ok: false, error: "Unknown item" }. A qty that isn't a whole number from 1 to 5
  // returns { ok: false, error: "Pick a quantity from 1 to 5" }. In both cases, save nothing.
  const order = addOrder({ itemId: id, qty, totalPaise: paise * qty });
  return { ok: true, orderId: order.id, totalPaise: order.totalPaise };
}
