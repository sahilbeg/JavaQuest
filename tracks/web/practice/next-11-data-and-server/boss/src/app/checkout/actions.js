"use server";

import { redirect } from "next/navigation";
import { createOrder } from "../../lib/orders.js";

/** The checkout form's action. The form sends name, phone, itemId, qty and a hidden requestId. */
export async function placeOrder(prevState, formData) {
  // TODO 3 (gold): the same form sent twice (the same requestId), at once or later, must make only one order,
  // and both sends must redirect to it. Different requestIds still make separate orders.
  const requestId = String(formData.get("requestId") ?? "");
  const details = {
    name: String(formData.get("name") ?? "").trim(),
    phone: String(formData.get("phone") ?? "").replace(/\s/g, ""),
    lines: [{ itemId: String(formData.get("itemId") ?? ""), qty: Number(formData.get("qty") ?? 1) }],
  };

  const order = await createOrder(details);
  redirect(`/orders/${order.id}`);
}
