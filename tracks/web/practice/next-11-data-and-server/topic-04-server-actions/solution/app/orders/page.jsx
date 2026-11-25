import { listOrders } from "../../lib/orders.js";
import { rupees } from "../../lib/money.js";
import { markPacked, cancelOrder } from "./actions.js";

export default async function OrdersPage() {
  const orders = listOrders();

  return (
    <main>
      <h1>Orders</h1>
      <ul>
        {orders.map((order) => (
          <li key={order.id}>
            <p>{`${order.id} · ${order.name} · ${rupees(order.totalPaise)} · ${order.status}`}</p>
            {order.status === "NEW" && (
              <>
                <form action={markPacked}>
                  <input type="hidden" name="orderId" value={order.id} />
                  <button type="submit">Mark packed</button>
                </form>
                <form action={cancelOrder.bind(null, order.id)}>
                  <button type="submit">Cancel</button>
                </form>
              </>
            )}
          </li>
        ))}
      </ul>
    </main>
  );
}
