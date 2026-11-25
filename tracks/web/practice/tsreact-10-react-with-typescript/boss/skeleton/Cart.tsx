import { useState } from "react";
import { NAMES, PRICES } from "./catalogue.ts";
import { rupees } from "./money.ts";

export interface CartLine {
  id: string;
  qty: number;
}

export interface CartProps {
  initialLines: CartLine[];
}

const DELIVERY_PAISE = 6000;
const FREE_DELIVERY_ABOVE = 99900;

export function Cart({ initialLines }: CartProps) {
  const [lines, setLines] = useState<CartLine[]>(initialLines);

  function remove(id: string) {
    setLines(lines.filter((line) => line.id !== id));
  }

  const subtotal = lines.reduce((sum, line) => sum + PRICES[line.id] * line.qty, 0);
  const count = lines.reduce((sum, line) => sum + line.qty, 0);
  const delivery = subtotal > FREE_DELIVERY_ABOVE ? 0 : DELIVERY_PAISE;

  return (
    <section className="cart">
      <h2>Your cart</h2>
      <p className="count">
        {count} {count === 1 ? "item" : "items"}
      </p>
      <ul className="cart-lines">
        {lines.map((line) => (
          <li key={line.id} className="cart-line">
            <span className="name">{`${NAMES[line.id]} × ${line.qty}`}</span>
            <span className="line-total">{rupees(PRICES[line.id] * line.qty)}</span>
            <button type="button" aria-label={`Remove ${NAMES[line.id]}`} onClick={() => remove(line.id)}>
              Remove
            </button>
          </li>
        ))}
      </ul>
      <p className="subtotal">Subtotal: {rupees(subtotal)}</p>
      <p className="delivery">Delivery: {delivery === 0 ? "Free" : rupees(delivery)}</p>
      <p className="total">Total: {rupees(subtotal + delivery)}</p>
    </section>
  );
}
