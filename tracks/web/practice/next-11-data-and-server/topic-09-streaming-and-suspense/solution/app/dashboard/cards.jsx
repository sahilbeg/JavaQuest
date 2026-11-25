import { use } from "react";
import { rupees } from "../../lib/money.js";

/** Today's sales, from a promise the page started. */
export function SalesCard({ salesPromise }) {
  const sales = use(salesPromise);
  return (
    <section>
      <h2>Today's sales</h2>
      <p>{`${sales.orders} orders, ${rupees(sales.totalPaise)}`}</p>
    </section>
  );
}

/** Items running low, from a promise the page started. */
export function LowStockCard({ stockPromise }) {
  const items = use(stockPromise);
  return (
    <section>
      <h2>Low stock</h2>
      {items.length === 0 ? (
        <p>Nothing is running low.</p>
      ) : (
        <ul>
          {items.map((item) => (
            <li key={item.id}>{`${item.name}: ${item.left} left`}</li>
          ))}
        </ul>
      )}
    </section>
  );
}
