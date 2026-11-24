import { useId, useState } from "react";

/** A title that shows or hides a panel, like the "Delivery and returns" answer on the help page. */
export function Disclosure({ title, children }) {
  const [open, setOpen] = useState(false);
  const panelId = useId();

  return (
    <div className="disclosure">
      <h3>
        <button type="button" aria-expanded={open} aria-controls={panelId} onClick={() => setOpen(!open)}>
          {title}
        </button>
      </h3>
      <div id={panelId} className="panel" hidden={!open}>
        {children}
      </div>
    </div>
  );
}
