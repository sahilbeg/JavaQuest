import { createRoot } from "react-dom/client";
import { Checkout } from "./Checkout.tsx";
import type { PlacedOrder } from "./Checkout.tsx";
import type { CartLine } from "./totals.ts";

const LINES: CartLine[] = [
  { code: "CST", name: "Coaster set", paise: 15000, qty: 2 },
  { code: "AMI", name: "Golu the elephant", paise: 45000, qty: 1 },
];

let nextId = 17;

/** A pretend server for the preview: each order gets the next id. */
function placeOrder(order: PlacedOrder): string {
  console.log("Order placed", order);
  return `order-${nextId++}`;
}

createRoot(document.getElementById("root")!).render(
  <main>
    <h1>Checkout</h1>
    <Checkout lines={LINES} placeOrder={placeOrder} />
  </main>,
);
