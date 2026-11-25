"use server";

import { revalidatePath } from "next/cache";
import { getOrder, setStatus, NEXT_STATUS } from "../../lib/orders.js";

/** Raju's Move to ... button: moves an order one step along. */
export async function advanceOrder(formData) {
  // TODO 3: read orderId from the form. Unknown: return { error: `No order ${orderId}.` }.
  // No NEXT_STATUS for its status: return { error: `${orderId} is already ${order.status}.` }.
  // Otherwise setStatus to the next status, revalidatePath("/orders"), and return { ok: true, status: next }.
  return { error: "Not built yet." };
}
