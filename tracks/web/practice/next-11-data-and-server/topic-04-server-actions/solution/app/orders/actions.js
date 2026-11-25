"use server";

import { revalidatePath } from "next/cache";
import { getOrder, setStatus } from "../../lib/orders.js";

/** Called by the Mark packed form, with its FormData. */
export async function markPacked(formData) {
  const orderId = formData.get("orderId");
  const order = getOrder(orderId);
  if (!order) return { error: `No order ${orderId}.` };
  if (order.status !== "NEW") return { error: `${orderId} is already ${order.status}.` };

  setStatus(orderId, "PACKED");
  revalidatePath("/orders");
  return { ok: true };
}

/** Called by the Cancel form as cancelOrder.bind(null, order.id), so the id comes first. */
export async function cancelOrder(orderId, formData) {
  const order = getOrder(orderId);
  if (!order) return { error: `No order ${orderId}.` };
  if (order.status !== "NEW") return { error: `${orderId} can't be cancelled once it's ${order.status}.` };

  setStatus(orderId, "CANCELLED");
  revalidatePath("/orders");
  return { ok: true };
}
