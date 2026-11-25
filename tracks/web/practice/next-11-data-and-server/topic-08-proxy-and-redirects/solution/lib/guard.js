import { notFound, redirect } from "next/navigation";
import { currentCustomer } from "./session.js";
import { findOrder } from "./orders.js";

/** The logged-in customer, or a redirect to the login page that comes back to path. */
export function requireCustomer(cookieStore, path) {
  const customer = currentCustomer(cookieStore);
  if (!customer) redirect(`/login?next=${path}`);
  return customer;
}

/** The customer's own order, or the not-found page. */
export function getOwnOrder(cookieStore, orderId) {
  const customer = requireCustomer(cookieStore, `/account/orders/${orderId}`);
  const order = findOrder(orderId);
  if (!order || order.phone !== customer.phone) notFound();
  return order;
}
