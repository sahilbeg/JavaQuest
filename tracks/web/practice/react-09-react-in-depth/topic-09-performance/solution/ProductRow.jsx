import { memo } from "react";
import { rupees } from "./money.js";

/** Every render of a row adds its item code here, so the checks (and the preview) can count renders. */
export const rowRenders = [];

export const ProductRow = memo(function ProductRow({ item, onAdd }) {
  rowRenders.push(item.code);
  return (
    <li>
      {item.name}, {rupees(item.paise)} <button onClick={() => onAdd(item)}>Add {item.name}</button>
    </li>
  );
});
