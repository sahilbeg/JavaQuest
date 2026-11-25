import type { CartLine } from "./items.ts";
import { rupees } from "./money.ts";

export interface CartSummaryProps {
  lines: readonly CartLine[];
  // TODO 5: Add a callback prop onRemove that takes the item's id and returns nothing.
}

export function CartSummary({ lines }: CartSummaryProps) {
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
            {/* TODO 5: A button with aria-label={`Remove ${line.item.name}`} that calls onRemove(line.item.id). */}
          </li>
        ))}
      </ul>
      <p className="cart-total">Total: {rupees(total)}</p>
    </section>
  );
}
