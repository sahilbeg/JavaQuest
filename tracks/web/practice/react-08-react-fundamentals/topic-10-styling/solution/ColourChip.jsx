import { HEX } from "./colours.js";
import { rupees } from "./money.js";

export function ColourChip({ colour, selected = false, onSelect }) {
  return (
    <button
      type="button"
      className={selected ? "chip chip--selected" : "chip"}
      aria-pressed={selected}
      onClick={() => onSelect(colour)}
    >
      <span className="swatch" style={{ backgroundColor: HEX[colour] }} />
      {colour}
    </button>
  );
}

export function PriceTag({ paise, onSale = false }) {
  return <p className={onSale ? "price price--sale" : "price"}>{rupees(paise)}</p>;
}
