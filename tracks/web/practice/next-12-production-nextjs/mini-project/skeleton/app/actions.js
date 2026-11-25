"use server";

import { redirect } from "next/navigation";
import { getItem, createOrder } from "../lib/store.js";
import { orderTotals } from "../lib/pricing.js";

// The checkout form sends a "line" field per cart line ("DC-1042:2"), plus name and phone.
// Used with useActionState, so it gets (prevState, formData) and returns the next state, { error }.
export async function placeOrder(prevState, formData) {
  // TODO 7: check, in this order, and return { error } for the first problem:
  //   no line fields: "Your cart is empty";
  //   a name that's empty after trimming: "Please enter your name";
  //   a phone that isn't 10 digits once spaces are removed: "Please enter a 10-digit phone number";
  //   a line whose item doesn't exist or whose qty isn't a whole number from 1 to 5: "Something in your cart can't be sold. Please check it.";
  //   createOrder returning null: "Some items just sold out. Please check your cart."
  // Use the store's prices, work out totals with orderTotals, save with createOrder, then redirect to /orders/<order id>.
  return { error: "Ordering isn't built yet" };
}
