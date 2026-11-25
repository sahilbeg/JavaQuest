import { listOrders, NEXT_STATUS } from "../../lib/orders.js";
import { rupees } from "../../lib/money.js";
import { advanceOrder } from "./actions.js";

/** Raju's orders page in the shop. */
export default async function OrdersPage() {
  const orders = listOrders();

  // TODO 5: under the heading, one <li key={order.id}> per order with
  // <p>{`${order.id} · ${order.name} · ${rupees(order.totalPaise)} · ${order.status}`}</p>.
  // When NEXT_STATUS[order.status] exists, add <form action={advanceOrder}> with
  // <input type="hidden" name="orderId" value={order.id} /> and a submit button "Move to PACKED" (the next status).
  // With no orders, show <p>No orders yet.</p>.
  return (
    <main>
      <h1>Orders</h1>
    </main>
  );
}
