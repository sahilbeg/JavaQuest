import { MAX_QTY } from "./cart.js";
import { formatRupees } from "./money.js";
import { cartTotals } from "./pricing.js";

const QUANTITIES = Array.from({ length: MAX_QTY }, (_, i) => i + 1);

/** The cart: one line per item, the totals, and the Checkout button. */
export function Cart({ lines, onSetQty, onRemove, onCheckout }) {
  const { subtotal, discount, delivery, total } = cartTotals(lines);

  // TODO 5: on each line, label the quantity select "Quantity for <name>" (the label is there, but not tied to the select),
  // make it show line.qty and call onSetQty(line.id, <the new number>), and name each Remove button "Remove <name>" and make it call onRemove(line.id).
  // In the totals, add "Diwali offer: -₹..." only when there is a discount, and "Delivery: Free" or "Delivery: ₹60.00".
  return (
    <section className="cart" aria-labelledby="cart-title">
      <h2 id="cart-title">Your cart</h2>
      {lines.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <ul className="lines">
          {lines.map((line) => (
            <li key={line.id}>
              <span className="line-name">{line.name}</span>
              <label className="visually-hidden">Quantity for {line.name}</label>
              <select defaultValue={line.qty}>
                {QUANTITIES.map((n) => (
                  <option key={n} value={n}>
                    {n}
                  </option>
                ))}
              </select>
              <span>{formatRupees(line.paise * line.qty)}</span>
              <button type="button">Remove</button>
            </li>
          ))}
        </ul>
      )}
      <div className="totals">
        <p>Subtotal: {formatRupees(subtotal)}</p>
        <p className="total">Total: {formatRupees(total)}</p>
      </div>
      <button type="button" disabled={lines.length === 0} onClick={onCheckout}>
        Checkout
      </button>
    </section>
  );
}
