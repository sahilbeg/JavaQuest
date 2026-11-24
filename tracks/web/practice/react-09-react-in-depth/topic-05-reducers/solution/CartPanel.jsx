import { useReducer } from "react";
import { cartReducer, emptyCart } from "./cartReducer.js";
import { rupees } from "./money.js";

/** Add buttons for each item, and the cart's lines with their own buttons. */
export function CartPanel({ items }) {
  const [cart, dispatch] = useReducer(cartReducer, emptyCart);

  const count = cart.lines.reduce((sum, line) => sum + line.qty, 0);
  const total = cart.lines.reduce((sum, line) => sum + line.paise * line.qty, 0);

  return (
    <section>
      <h2>Cart ({count})</h2>
      <div>
        {items.map((item) => (
          <button key={item.code} onClick={() => dispatch({ type: "added", item })}>
            Add {item.name}
          </button>
        ))}
      </div>
      <ul>
        {cart.lines.map((line) => (
          <li key={line.code}>
            {line.name} × {line.qty}{" "}
            <button onClick={() => dispatch({ type: "quantityChanged", code: line.code, qty: line.qty - 1 })}>
              One less {line.name}
            </button>{" "}
            <button onClick={() => dispatch({ type: "removed", code: line.code })}>Remove {line.name}</button>
          </li>
        ))}
      </ul>
      <p>Total: {rupees(total)}</p>
      {cart.lines.length > 0 && <button onClick={() => dispatch({ type: "cleared" })}>Empty the cart</button>}
    </section>
  );
}
