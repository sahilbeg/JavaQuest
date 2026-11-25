import { useCallback, useMemo, useState } from "react";
import type { ChangeEvent } from "react";
import { OrderRow } from "./OrderRow.tsx";
import { filterOrders } from "./orders.ts";
import type { PastOrder } from "./orders.ts";

/** Raju's order history: a search box, the orders that match, and the one he opened. */
export function OrderHistory({ orders }: { orders: PastOrder[] }) {
  const [query, setQuery] = useState<string>("");
  const [opened, setOpened] = useState<string | null>(null);

  const shown = useMemo(() => filterOrders(orders, query), [orders, query]);

  const handleOpen = useCallback((id: string) => setOpened(id), []);

  return (
    <section>
      <label>
        Search orders <input value={query} onChange={(e: ChangeEvent<HTMLInputElement>) => setQuery(e.currentTarget.value)} />
      </label>
      <p>{opened ? `Opened ${opened}` : "No order open"}</p>
      <ul>
        {shown.map((order) => (
          <OrderRow key={order.id} order={order} onOpen={handleOpen} />
        ))}
      </ul>
    </section>
  );
}
