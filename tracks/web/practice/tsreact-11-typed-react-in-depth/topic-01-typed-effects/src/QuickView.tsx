import { useEffect } from "react";
import { rupees } from "./money.ts";

export interface QuickViewProps {
  name: string;
  paise: number;
  open: boolean;
  onClose: () => void;
}

/** A small pop-up with an item's name and price. Escape closes it. */
export function QuickView({ name, paise, open, onClose }: QuickViewProps) {
  // TODO 1: while open is true, listen for "keydown" on window with a named handler (event: KeyboardEvent).
  //         When event.key is "Escape", call onClose().
  // TODO 2: return a cleanup that removes that same handler. List [open, onClose] as the dependencies.

  if (!open) return null;
  return (
    <div role="dialog" aria-label={name}>
      <h2>{name}</h2>
      <p>{rupees(paise)}</p>
      <button onClick={onClose}>Close</button>
    </div>
  );
}
