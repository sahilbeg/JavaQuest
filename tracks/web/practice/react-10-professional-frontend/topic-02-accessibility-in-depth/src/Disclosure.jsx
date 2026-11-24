import { useId, useState } from "react";

/** A title that shows or hides a panel, like the "Delivery and returns" answer on the help page. */
export function Disclosure({ title, children }) {
  const [open, setOpen] = useState(false);
  const panelId = useId();

  return (
    <div className="disclosure">
      <h3>
        {/* TODO 1: make this a real <button type="button">, with aria-expanded={open} and aria-controls={panelId}. */}
        <div className="disclosure-title" onClick={() => setOpen(!open)}>
          {title}
        </div>
      </h3>
      {/* TODO 2: always render the panel, with id={panelId} and hidden={!open}, so aria-controls always points at it. */}
      {open && <div className="panel">{children}</div>}
    </div>
  );
}
