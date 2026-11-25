"use server";

import { revalidatePath } from "next/cache";
import { getOrder, setStatus, NEXT_STATUS } from "../../lib/orders.js";

/** Raju's Move to ... button: moves an order one step along. */
export async function advanceOrder(formData) {
  const orderId = formData.get("orderId");
  const order = getOrder(orderId);
  if (!order) return { error: `No order ${orderId}.` };
  const next = NEXT_STATUS[order.status];
  if (!next) return { error: `${orderId} is already ${order.status}.` };

  setStatus(orderId, next);
  revalidatePath("/orders");
  return { ok: true, status: next };
}
