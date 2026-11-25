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

interface Found {
  name: string;
  paise: number;
}

const DELIVERY_PAISE = 6000;
const FREE_DELIVERY_ABOVE = 99900;

/** The item for an id, or undefined when the catalogue doesn't really have it. */
function findItem(id: string): Found | undefined {
  if (!Object.hasOwn(NAMES, id) || !Object.hasOwn(PRICES, id)) {
    return undefined;
  }
  return { name: NAMES[id], paise: PRICES[id] };
}

export function Cart({ initialLines }: CartProps) {
  const [lines, setLines] = useState<CartLine[]>(initialLines);

  function remove(id: string) {
    setLines(lines.filter((line) => line.id !== id));
  }

  const rows = lines.map((line) => ({ line, item: findItem(line.id) }));
  const subtotal = rows.reduce((sum, row) => sum + (row.item ? row.item.paise * row.line.qty : 0), 0);
  const count = rows.reduce((sum, row) => sum + (row.item ? row.line.qty : 0), 0);
  const delivery = subtotal > FREE_DELIVERY_ABOVE ? 0 : DELIVERY_PAISE;

  return (
    <section className="cart">
      <h2>Your cart</h2>
      <p className="count">
        {count} {count === 1 ? "item" : "items"}
      </p>
      <ul className="cart-lines">
        {rows.map(({ line, item }) =>
          item === undefined ? (
            <li key={line.id} className="cart-line missing">
              <span className="name">{line.id} is no longer available</span>
              <button type="button" aria-label={`Remove ${line.id}`} onClick={() => remove(line.id)}>
                Remove
              </button>
            </li>
          ) : (
            <li key={line.id} className="cart-line">
              <span className="name">{`${item.name} × ${line.qty}`}</span>
              <span className="line-total">{rupees(item.paise * line.qty)}</span>
              <button type="button" aria-label={`Remove ${item.name}`} onClick={() => remove(line.id)}>
                Remove
              </button>
            </li>
          ),
        )}
      </ul>
      <p className="subtotal">Subtotal: {rupees(subtotal)}</p>
      <p className="delivery">Delivery: {delivery === 0 ? "Free" : rupees(delivery)}</p>
      <p className="total">Total: {rupees(subtotal + delivery)}</p>
    </section>
  );
}
