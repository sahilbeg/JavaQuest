import { HEX } from "./colours.js";
import { rupees } from "./money.js";

export function ColourChip({ colour, selected = false, onSelect }) {
  // TODO 1: Build the className from selected: "chip" normally, "chip chip--selected" when selected.
  //         Also set aria-pressed={selected}, so screen readers know which chip is picked.
  // TODO 2: Give the swatch an inline style whose backgroundColor is HEX[colour].
  return (
    <button type="button" className="chip" onClick={() => onSelect(colour)}>
      <span className="swatch" />
      {colour}
    </button>
  );
}

// TODO 3: The className is "price" normally, and "price price--sale" when onSale is true.
export function PriceTag({ paise, onSale = false }) {
  return <p className="price">{rupees(paise)}</p>;
}
