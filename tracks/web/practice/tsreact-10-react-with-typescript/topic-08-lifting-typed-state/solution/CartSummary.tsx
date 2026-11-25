import type { CartLine } from "./items.ts";
import { rupees } from "./money.ts";

export interface CartSummaryProps {
  lines: readonly CartLine[];
  onRemove: (id: string) => void;
}

export function CartSummary({ lines, onRemove }: CartSummaryProps) {
  if (lines.length === 0) {
    return <p className="cart-empty">Your cart is empty</p>;
  }

  const total = lines.reduce((sum, line) => sum + line.item.paise * line.qty, 0);
  return (
    <section className="cart-summary">
      <ul>
        {lines.map((line) => (
          <li key={line.item.id} className="cart-line">
            <span className="line-name">
              {line.item.name} × {line.qty}
            </span>
            <button type="button" aria-label={`Remove ${line.item.name}`} onClick={() => onRemove(line.item.id)}>
              Remove
            </button>
          </li>
        ))}
      </ul>
      <p className="cart-total">Total: {rupees(total)}</p>
    </section>
  );
}
