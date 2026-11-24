import { memo } from "react";
import { rupees } from "./money.js";

/** Every render of a row adds its item code here, so the checks (and the preview) can count renders. */
export const rowRenders = [];

// TODO 1: wrap this component in memo(...), so React skips it when its props are the same as last time.
//         For example: export const ProductRow = memo(function ProductRow({ item, onAdd }) { ... });
export function ProductRow({ item, onAdd }) {
  rowRenders.push(item.code);
  return (
    <li>
      {item.name}, {rupees(item.paise)} <button onClick={() => onAdd(item)}>Add {item.name}</button>
    </li>
  );
}
