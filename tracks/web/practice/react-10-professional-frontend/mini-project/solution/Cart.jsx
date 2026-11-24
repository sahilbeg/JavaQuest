import { MAX_QTY } from "./cart.js";
import { formatRupees } from "./money.js";
import { cartTotals } from "./pricing.js";

const QUANTITIES = Array.from({ length: MAX_QTY }, (_, i) => i + 1);

/** The cart: one line per item, the totals, and the Checkout button. */
export function Cart({ lines, onSetQty, onRemove, onCheckout }) {
  const { subtotal, discount, delivery, total } = cartTotals(lines);

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
              <label htmlFor={`qty-${line.id}`} className="visually-hidden">
                Quantity for {line.name}
              </label>
              <select id={`qty-${line.id}`} value={line.qty} onChange={(event) => onSetQty(line.id, Number(event.target.value))}>
                {QUANTITIES.map((n) => (
                  <option key={n} value={n}>
                    {n}
                  </option>
                ))}
              </select>
              <span>{formatRupees(line.paise * line.qty)}</span>
              <button type="button" aria-label={`Remove ${line.name}`} onClick={() => onRemove(line.id)}>
                Remove
              </button>
            </li>
          ))}
        </ul>
      )}
      <div className="totals">
        <p>Subtotal: {formatRupees(subtotal)}</p>
        {discount > 0 && <p>Diwali offer: -{formatRupees(discount)}</p>}
        <p>Delivery: {delivery === 0 ? "Free" : formatRupees(delivery)}</p>
        <p className="total">Total: {formatRupees(total)}</p>
      </div>
      <button type="button" disabled={lines.length === 0} onClick={onCheckout}>
        Checkout
      </button>
    </section>
  );
}
