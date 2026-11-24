import { createRoot } from "react-dom/client";
import { useCart } from "./useCart.js";

const ITEMS = [
  { code: "CST", name: "Coaster set", paise: 15000 },
  { code: "KEY", name: "Keychain", paise: 12000 },
  { code: "AMI", name: "Amigurumi elephant", paise: 45000 },
];

/** Paise to rupees for the page: 129900 becomes "₹1,299.00". */
function rupees(paise) {
  const whole = String(Math.floor(paise / 100)).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  return `₹${whole}.${String(paise % 100).padStart(2, "0")}`;
}

function Shop() {
  const cart = useCart();
  return (
    <main>
      <h1>Dhaaga &amp; Co.</h1>
      <ProductGrid onAdd={cart.add} />
      <CartSummary cart={cart} />
    </main>
  );
}

function ProductGrid({ onAdd }) {
  return (
    <ul>
      {ITEMS.map((item) => (
        <li key={item.code}>
          {item.name}, {rupees(item.paise)}{" "}
          <button onClick={() => onAdd(item)}>Add</button>{" "}
          <button
            onClick={() => {
              onAdd(item);
              onAdd(item);
            }}
          >
            Buy 2
          </button>
        </li>
      ))}
    </ul>
  );
}

function CartSummary({ cart }) {
  return (
    <section>
      <h2>Cart ({cart.count})</h2>
      <ul>
        {cart.lines.map((line) => (
          <li key={line.code}>
            {line.name} × {line.qty} <button onClick={() => cart.remove(line.code)}>Remove</button>
          </li>
        ))}
      </ul>
      <p>Total: {rupees(cart.total)}</p>
    </section>
  );
}

createRoot(document.getElementById("root")).render(<Shop />);
