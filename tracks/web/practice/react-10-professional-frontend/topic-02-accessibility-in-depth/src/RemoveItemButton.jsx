import { useEffect, useId, useRef, useState } from "react";

/** A cart line's Remove button. It asks first, in a small modal dialog. */
export function RemoveItemButton({ itemName, onRemove }) {
  const [open, setOpen] = useState(false);
  const openerRef = useRef(null);
  const cancelRef = useRef(null);
  const titleId = useId();

  // TODO 3: when the dialog opens, move focus to the Cancel button. Use an effect that runs when open changes.

  function close() {
    setOpen(false);
    // TODO 4: put focus back on the Remove button that opened the dialog (openerRef).
  }

  return (
    <>
      {/* TODO 5: give this button an aria-label that names the item, like "Remove Golu the elephant". */}
      <button type="button" ref={openerRef} onClick={() => setOpen(true)}>
        Remove
      </button>
      {open && (
        <div className="backdrop">
          {/* TODO 6: make this div a modal dialog: role="dialog", aria-modal="true", aria-labelledby={titleId}, and call close() when Escape is pressed. */}
          <div className="dialog">
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
