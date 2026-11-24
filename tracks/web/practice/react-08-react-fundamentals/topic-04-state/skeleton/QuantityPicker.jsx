import { useState } from "react";
import { rupees } from "./money.js";

export function QuantityPicker({ name, paise, stock }) {
  // TODO 1: This plain variable starts again at 1 on every render, and changing it never redraws the page.
  //         Replace it with state: const [qty, setQty] = useState(1);
  let qty = 1;

  return (
    <div className="picker">
      <h3>{name}</h3>
      {/* TODO 2: Make "-" set qty to one less with the setter, and disable it when qty is 1. */}
      <button type="button" aria-label="One less" onClick={() => { qty = qty - 1; }}>
        -
      </button>
      <span className="qty">{qty}</span>
      {/* TODO 3: Make "+" set qty to one more with the setter, and disable it when qty equals stock. */}
      <button type="button" aria-label="One more" onClick={() => { qty = qty + 1; }}>
        +
      </button>
      {/* TODO 4: Show the price of qty items, not of one. */}
      <p className="line-total">{rupees(paise)}</p>
    </div>
  );
}
