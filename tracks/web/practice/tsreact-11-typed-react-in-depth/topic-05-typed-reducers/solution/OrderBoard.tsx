import { useReducer } from "react";
import type { Dispatch } from "react";
import { ordersReducer } from "./orders.ts";
import type { Order, OrderAction } from "./orders.ts";

function OrderRow({ order, dispatch }: { order: Order; dispatch: Dispatch<OrderAction> }) {
  const canCancel = order.status === "new" || order.status === "packed";
  return (
    <li>
      <span>
        {order.id} for {order.customer}: {order.status}
      </span>{" "}
      {order.status === "new" && <button onClick={() => dispatch({ type: "packed", id: order.id })}>Pack {order.id}</button>}
      {order.status === "packed" && <button onClick={() => dispatch({ type: "shipped", id: order.id })}>Ship {order.id}</button>}
      {canCancel && <button onClick={() => dispatch({ type: "cancelled", id: order.id })}>Cancel {order.id}</button>}
    </li>
  );
}

/** Raju's board of today's orders. */
export function OrderBoard({ start }: { start: Order[] }) {
  const [orders, dispatch] = useReducer(ordersReducer, start);

  return (
    <section>
      <h2>Today's orders</h2>
      <ul>
        {orders.map((order) => (
          <OrderRow key={order.id} order={order} dispatch={dispatch} />
        ))}
      </ul>
    </section>
  );
}
