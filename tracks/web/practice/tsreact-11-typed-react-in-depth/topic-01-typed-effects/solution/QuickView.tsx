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
  useEffect(() => {
    if (!open) return;
    function handleKey(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, [open, onClose]);

  if (!open) return null;
  return (
    <div role="dialog" aria-label={name}>
      <h2>{name}</h2>
      <p>{rupees(paise)}</p>
      <button onClick={onClose}>Close</button>
    </div>
  );
}
