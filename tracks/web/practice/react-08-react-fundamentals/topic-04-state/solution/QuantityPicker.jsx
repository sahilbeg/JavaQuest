import { useState } from "react";
import { rupees } from "./money.js";

export function QuantityPicker({ name, paise, stock }) {
  const [qty, setQty] = useState(1);

  return (
    <div className="picker">
      <h3>{name}</h3>
      <button type="button" aria-label="One less" disabled={qty === 1} onClick={() => setQty(qty - 1)}>
        -
      </button>
      <span className="qty">{qty}</span>
      <button type="button" aria-label="One more" disabled={qty === stock} onClick={() => setQty(qty + 1)}>
        +
      </button>
      <p className="line-total">{rupees(paise * qty)}</p>
    </div>
  );
}
