import { getOrder } from "../../../../lib/orders.js";
import { rupees } from "../../../../lib/money.js";

/** GET /api/orders/order-20, for the WhatsApp bot. */
export async function GET(request, { params }) {
  // TODO 4: const { id } = await params; look the order up with getOrder.
  // Answer only { id, status, total: rupees(order.totalPaise) }, or 404 { error: `No order ${id}.` }.
  return Response.json({ error: "Not built yet." }, { status: 501 });
}
