"use server";

import { revalidatePath } from "next/cache";
import { getOrder, setStatus } from "../../lib/orders.js";

/** Called by the Mark packed form, with its FormData. */
export async function markPacked(formData) {
  // TODO 1: read orderId with formData.get("orderId") and look it up with getOrder.
  // Unknown order: return { error: `No order ${orderId}.` }.
  // Not NEW: return { error: `${orderId} is already ${order.status}.` } and change nothing.
  // TODO 2: set the status to "PACKED", call revalidatePath("/orders"), and return { ok: true }.
  return { error: "Not built yet." };
}

/** Called by the Cancel form as cancelOrder.bind(null, order.id), so the id comes first. */
export async function cancelOrder(orderId, formData) {
  // TODO 3: unknown order: { error: `No order ${orderId}.` }.
  // Not NEW: { error: `${orderId} can't be cancelled once it's ${order.status}.` }.
  // Otherwise set it to "CANCELLED", call revalidatePath("/orders") and return { ok: true }.
  return { error: "Not built yet." };
}
