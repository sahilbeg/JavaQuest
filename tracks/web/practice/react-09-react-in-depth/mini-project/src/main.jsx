import { createRoot } from "react-dom/client";
import { Checkout } from "./Checkout.jsx";

const LINES = [
  { code: "CST", name: "Coaster set", paise: 15000, qty: 2 },
  { code: "AMI", name: "Amigurumi elephant", paise: 45000, qty: 1 },
];

// A pretend order service for the preview: each order gets the next number.
let nextOrder = 17;
function placeOrder() {
  const id = `order-${nextOrder}`;
  nextOrder += 1;
  return id;
}

createRoot(document.getElementById("root")).render(
  <main>
    <h1>Checkout</h1>
    <Checkout lines={LINES} placeOrder={placeOrder} />
  </main>,
);
