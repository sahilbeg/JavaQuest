"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { findItem } from "../../lib/catalogue.js";
import { createOrder } from "../../lib/orders.js";

/** The customer's order form, used with useActionState. */
export async function placeOrder(prevState, formData) {
  // TODO 2: read name, phone, itemId and qty as text into values, and check them:
  //   "Please enter your name.", "Phone needs 10 digits, like 98765 43210.",
  //   "Please choose an item.", "Choose between 1 and 5."
  // Any mistake: return { errors, values }.
  // Otherwise createOrder({ name: trimmed, phone: digits only, lines: [{ itemId, qty: as a number }] }),
  // then revalidatePath("/orders"), and last of all redirect(`/order/${order.id}`).
  return { errors: {}, values: {} };
}
