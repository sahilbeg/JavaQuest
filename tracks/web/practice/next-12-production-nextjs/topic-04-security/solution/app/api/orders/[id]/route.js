import { findOrder } from "../../../../lib/orders.js";
import { customerFor, sessionFromCookie } from "../../../../lib/sessions.js";

// GET /api/orders/order-17: the order tracking page asks this for one order's status.
export async function GET(request, { params }) {
  const { id } = await params;
  const customerId = customerFor(sessionFromCookie(request.headers.get("cookie")));
  if (!customerId) {
    return Response.json({ error: "Please log in" }, { status: 401 });
  }
  const order = findOrder(id);
  if (!order || order.customerId !== customerId) {
    return Response.json({ error: "Order not found" }, { status: 404 });
  }
  return Response.json({ id: order.id, itemId: order.itemId, qty: order.qty, totalPaise: order.totalPaise, status: order.status });
}
