"use server";

import { getItem } from "../lib/catalogue.js";
import { addOrder } from "../lib/orders.js";

// The "Buy now" form sends two fields: id and qty.
export async function placeOrder(formData) {
  const item = await getItem(String(formData.get("id")));
  const qty = Number(formData.get("qty"));
  if (!item) {
    return { ok: false, error: "Unknown item" };
  }
  if (!Number.isInteger(qty) || qty < 1 || qty > 5) {
    return { ok: false, error: "Pick a quantity from 1 to 5" };
  }
  const order = addOrder({ itemId: item.id, qty, totalPaise: item.paise * qty });
  return { ok: true, orderId: order.id, totalPaise: order.totalPaise };
}
