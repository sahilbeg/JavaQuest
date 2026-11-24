import { useEffect, useState } from "react";

/** Shows one order's status, fetched from /api/orders/<orderId>. */
export function OrderStatus({ orderId }) {
  const [order, setOrder] = useState(null);
  const [error, setError] = useState("");

  // TODO 1: in an effect, fetch `/api/orders/${orderId}` and keep the answer's JSON in order.
  // TODO 2: if the answer is not ok, or fetch throws, set error to `We couldn't load ${orderId}.`
  // TODO 3: run the effect again when orderId changes, clearing the old order and error first.
  // TODO 4: return a cleanup that makes the old request's answer be ignored.

  if (error) return <p role="alert">{error}</p>;
  if (order === null) return <p>Loading {orderId}...</p>;
  return (
    <section>
      <h2>{order.id}</h2>
      <p>Status: {order.status}</p>
    </section>
  );
}
