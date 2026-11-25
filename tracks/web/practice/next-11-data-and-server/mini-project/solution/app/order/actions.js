"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { findItem } from "../../lib/catalogue.js";
import { createOrder } from "../../lib/orders.js";

/** The customer's order form, used with useActionState. */
export async function placeOrder(prevState, formData) {
  const values = {
    name: String(formData.get("name") ?? ""),
    phone: String(formData.get("phone") ?? ""),
    itemId: String(formData.get("itemId") ?? ""),
    qty: String(formData.get("qty") ?? ""),
  };

  const errors = {};
  if (values.name.trim() === "") errors.name = "Please enter your name.";
  const digits = values.phone.replace(/\s/g, "");
  if (!/^\d{10}$/.test(digits)) errors.phone = "Phone needs 10 digits, like 98765 43210.";
  if (!findItem(values.itemId)) errors.itemId = "Please choose an item.";
  const qty = Number(values.qty);
  if (!Number.isInteger(qty) || qty < 1 || qty > 5) errors.qty = "Choose between 1 and 5.";
  if (Object.keys(errors).length > 0) return { errors, values };

  const order = createOrder({ name: values.name.trim(), phone: digits, lines: [{ itemId: values.itemId, qty }] });
  revalidatePath("/orders");
  redirect(`/order/${order.id}`);
}
