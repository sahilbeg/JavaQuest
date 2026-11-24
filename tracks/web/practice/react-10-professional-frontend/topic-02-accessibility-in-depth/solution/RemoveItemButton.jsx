import { useEffect, useId, useRef, useState } from "react";

/** A cart line's Remove button. It asks first, in a small modal dialog. */
export function RemoveItemButton({ itemName, onRemove }) {
  const [open, setOpen] = useState(false);
  const openerRef = useRef(null);
  const cancelRef = useRef(null);
  const titleId = useId();

  useEffect(() => {
    if (open) cancelRef.current.focus();
  }, [open]);

  function close() {
    setOpen(false);
    openerRef.current.focus();
  }

  return (
    <>
      <button type="button" ref={openerRef} aria-label={`Remove ${itemName}`} onClick={() => setOpen(true)}>
        Remove
      </button>
      {open && (
        <div className="backdrop">
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby={titleId}
            className="dialog"
            onKeyDown={(event) => {
              if (event.key === "Escape") close();
            }}
          >
            <h2 id={titleId}>Remove {itemName}?</h2>
            <p>You can add it again from the catalogue.</p>
            <button type="button" ref={cancelRef} onClick={close}>
              Cancel
            </button>{" "}
            <button
              type="button"
              onClick={() => {
                onRemove();
                close();
              }}
            >
              Yes, remove
            </button>
          </div>
        </div>
      )}
    </>
  );
}
