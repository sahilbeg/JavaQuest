import { findOrder } from "../../../../lib/orders.js";
import { customerFor, sessionFromCookie } from "../../../../lib/sessions.js";

// GET /api/orders/order-17: the order tracking page asks this for one order's status.
export async function GET(request, { params }) {
  const { id } = await params;
  // TODO 3: find the customer from the request's "cookie" header with sessionFromCookie and customerFor.
  // No customer: 401 with { error: "Please log in" }. An order that belongs to someone else: the same 404 as a missing one.
  const order = findOrder(id);
  if (!order) {
    return Response.json({ error: "Order not found" }, { status: 404 });
  }
  // TODO 4: this sends everything, including the phone number and address. Send only id, itemId, qty, totalPaise and status.
  return Response.json(order);
}
