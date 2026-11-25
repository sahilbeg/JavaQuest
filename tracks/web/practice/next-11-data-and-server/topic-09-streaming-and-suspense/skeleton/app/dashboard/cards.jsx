import { use } from "react";
import { rupees } from "../../lib/money.js";

/** Today's sales, from a promise the page started. */
export function SalesCard({ salesPromise }) {
  // TODO 1: const sales = use(salesPromise);
  // then return <section> with <h2>Today's sales</h2> and <p>{`${sales.orders} orders, ${rupees(sales.totalPaise)}`}</p>.
  return null;
}

/** Items running low, from a promise the page started. */
export function LowStockCard({ stockPromise }) {
  // TODO 2: const items = use(stockPromise);
  // then return <section> with <h2>Low stock</h2> and one <li key={item.id}> per item, like "Golu the elephant: 2 left".
  // An empty list shows <p>Nothing is running low.</p> instead of the list.
  return null;
}
