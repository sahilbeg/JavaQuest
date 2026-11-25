"use server";

import { redirect } from "next/navigation";
import { findItem } from "../../lib/catalogue.js";
import { createOrder } from "../../lib/orders.js";

/** Used with useActionState, so the previous state comes first. */
export async function placeOrder(prevState, formData) {
  // TODO 1: read name, phone, itemId and qty from formData, as text, into a values object.
  const values = { name: "", phone: "", itemId: "", qty: "" };

  // TODO 2: collect a message per wrong field in an errors object:
  //   name empty after trim:              "Please enter your name."
  //   phone not 10 digits without spaces: "Phone needs 10 digits, like 98765 43210."
  //   itemId not found with findItem:     "Please choose an item."
  //   qty not a whole number from 1 to 5: "Choose between 1 and 5."
  // If there are any, return { errors, values } and save nothing.
  const errors = {};

  // TODO 3: createOrder({ name: trimmed, phone: digits only, itemId, qty: as a number }),
  // then redirect(`/order/${order.id}`). Keep redirect outside any try.
  return { errors, values };
}
