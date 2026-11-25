import { useCallback, useMemo, useState } from "react";
import type { ChangeEvent } from "react";
import { OrderRow } from "./OrderRow.tsx";
import { filterOrders } from "./orders.ts";
import type { PastOrder } from "./orders.ts";

/** Raju's order history: a search box, the orders that match, and the one he opened. */
export function OrderHistory({ orders }: { orders: PastOrder[] }) {
  const [query, setQuery] = useState<string>("");
  const [opened, setOpened] = useState<string | null>(null);

  // TODO 2: keep the result with useMemo, so it is worked out again only when orders or query change.
  const shown = filterOrders(orders, query);

  // TODO 3: keep this the same function between renders with useCallback.
  const handleOpen = (id: string) => setOpened(id);

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
