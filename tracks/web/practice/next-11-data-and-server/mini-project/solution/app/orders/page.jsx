import { listOrders, NEXT_STATUS } from "../../lib/orders.js";
import { rupees } from "../../lib/money.js";
import { advanceOrder } from "./actions.js";

/** Raju's orders page in the shop. */
export default async function OrdersPage() {
  const orders = listOrders();

  return (
    <main>
      <h1>Orders</h1>
      {orders.length === 0 ? (
        <p>No orders yet.</p>
      ) : (
        <ul>
          {orders.map((order) => {
            const next = NEXT_STATUS[order.status];
            return (
              <li key={order.id}>
                <p>{`${order.id} · ${order.name} · ${rupees(order.totalPaise)} · ${order.status}`}</p>
                {next && (
                  <form action={advanceOrder}>
                    <input type="hidden" name="orderId" value={order.id} />
                    <button type="submit">{`Move to ${next}`}</button>
                  </form>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </main>
  );
}
