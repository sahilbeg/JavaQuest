import { getOrder } from "../../../../lib/orders.js";
import { rupees } from "../../../../lib/money.js";

/** GET /api/orders/order-20, for the WhatsApp bot. */
export async function GET(request, { params }) {
  const { id } = await params;
  const order = getOrder(id);
  if (!order) return Response.json({ error: `No order ${id}.` }, { status: 404 });
  return Response.json({ id: order.id, status: order.status, total: rupees(order.totalPaise) });
}
