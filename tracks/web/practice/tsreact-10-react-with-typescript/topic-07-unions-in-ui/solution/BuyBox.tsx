import { StockBadge } from "./StockBadge.tsx";
import type { Stock } from "./StockBadge.tsx";

export interface BuyBoxProps {
  name: string;
  stock: Stock;
  onAdd: () => void;
}

export function BuyBox({ name, stock, onAdd }: BuyBoxProps) {
  return (
    <div className="buy-box">
      <h3>{name}</h3>
      <StockBadge stock={stock} />
      <button type="button" disabled={stock.kind === "sold-out"} onClick={onAdd}>
        Add to cart
      </button>
      {stock.kind === "made-to-order" && <p className="note">Crocheted for you after you order.</p>}
    </div>
  );
}
