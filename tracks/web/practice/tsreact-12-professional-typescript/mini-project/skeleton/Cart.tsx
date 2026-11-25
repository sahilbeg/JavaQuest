import { MAX_QTY } from "./cart.ts";
import { formatRupees } from "./money.ts";
import { cartTotals } from "./pricing.ts";
import type { CartLine } from "./types.ts";

const QUANTITIES = Array.from({ length: MAX_QTY }, (_, index) => index + 1);

export interface CartProps {
  lines: readonly CartLine[];
  onSetQty: (id: string, qty: number) => void;
  onRemove: (id: string) => void;
  onCheckout: () => void;
}

/** The cart: one line per item, the totals, and the Checkout button. */
export function Cart({ lines, onSetQty, onRemove, onCheckout }: CartProps) {
  const { subtotal, discount, delivery, total } = cartTotals(lines);

  // TODO 5: an empty cart says "Your cart is empty." and its Checkout button is disabled.
  // Each line needs a select of QUANTITIES labelled "Quantity for <name>" (it calls onSetQty with a number)
  // and a Remove button with aria-label="Remove <name>". Add "Diwali offer: -₹..." (only when discount > 0)
  // and "Delivery: ₹60.00" or "Delivery: Free" between the subtotal and the total.
  return (
    <section className="cart" aria-labelledby="cart-title">
      <h2 id="cart-title">Your cart</h2>
      <ul className="lines">
        {lines.map((line) => (
          <li key={line.id}>
            <span className="line-name">{line.name}</span>
            <span>{formatRupees(line.paise * line.qty)}</span>
          </li>
        ))}
      </ul>
      <div className="totals">
        <p>Subtotal: {formatRupees(subtotal)}</p>
        <p className="total">Total: {formatRupees(total)}</p>
      </div>
      <button type="button" onClick={onCheckout}>
        Checkout
      </button>
    </section>
  );
}
