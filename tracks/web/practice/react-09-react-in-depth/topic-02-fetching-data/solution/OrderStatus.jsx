import { useEffect, useState } from "react";

/** Shows one order's status, fetched from /api/orders/<orderId>. */
export function OrderStatus({ orderId }) {
  const [order, setOrder] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;
    setOrder(null);
    setError("");

    async function load() {
      try {
        const res = await fetch(`/api/orders/${orderId}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!ignore) setOrder(data);
      } catch {
        if (!ignore) setError(`We couldn't load ${orderId}.`);
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, [orderId]);

  if (error) return <p role="alert">{error}</p>;
  if (order === null) return <p>Loading {orderId}...</p>;
  return (
    <section>
      <h2>{order.id}</h2>
      <p>Status: {order.status}</p>
    </section>
  );
}
