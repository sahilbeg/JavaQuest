import { notFound, redirect } from "next/navigation";
import { currentCustomer } from "./session.js";
import { findOrder } from "./orders.js";

/** The logged-in customer, or a redirect to the login page that comes back to path. */
export function requireCustomer(cookieStore, path) {
  // TODO 4: const customer = currentCustomer(cookieStore);
  // with no customer, redirect(`/login?next=${path}`). Otherwise return the customer.
  return null;
}

/** The customer's own order, or the not-found page. */
export function getOwnOrder(cookieStore, orderId) {
  // TODO 5: requireCustomer(cookieStore, `/account/orders/${orderId}`) first.
  // Then findOrder(orderId), and notFound() when it's missing or its phone isn't the customer's.
  return null;
}
