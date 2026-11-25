// Cart changes that show at once and are saved in the background, with undo for removals.
// The state looks like { lines: [{ id: "DC-1042", qty: 1 }], undo: null | id, error: null | text }.

export const UNDO_MS = 5000;
export const SAVE_FAILED = "Couldn't update your cart. Please try again.";
export const REMOVE_FAILED = "Couldn't remove that item. It's back in your cart.";

/** A new array with line put back at index. The old array is not changed. */
export function insertAt(lines, line, index) {
  return [...lines.slice(0, index), line, ...lines.slice(index)];
}

/** Shows the new qty at once, then saves it. If saving fails, puts the old qty back. Never throws. */
export async function changeQty(store, id, qty) {
  const before = store.getState().lines.find((line) => line.id === id);
  if (before === undefined) {
    return;
  }
  const setQty = (value) =>
    store.setState((state) => ({ ...state, lines: state.lines.map((line) => (line.id === id ? { ...line, qty: value } : line)) }));

  setQty(qty);
  try {
    const response = await fetch(`/api/cart/${id}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ qty }),
    });
    if (!response.ok) {
      throw new Error(`Save failed: ${response.status}`);
    }
  } catch {
    setQty(before.qty);
    store.setState((state) => ({ ...state, error: SAVE_FAILED }));
  }
}

/**
 * Takes the line out at once and offers undo. After ms, tells the server with DELETE.
 * Returns { undo, finished }: undo() puts the line back, and finished settles once the removal is undone or done.
 */
export function removeWithUndo(store, id, ms = UNDO_MS) {
  const index = store.getState().lines.findIndex((line) => line.id === id);
  if (index === -1) {
    return { undo() {}, finished: Promise.resolve() };
  }
  const line = store.getState().lines[index];
  let open = true;
  let finish;
  const finished = new Promise((resolve) => {
    finish = resolve;
  });

  store.setState((state) => ({ ...state, lines: state.lines.filter((l) => l.id !== id), undo: id }));

  const timer = setTimeout(async () => {
    open = false;
    store.setState((state) => ({ ...state, undo: null }));
    try {
      const response = await fetch(`/api/cart/${id}`, { method: "DELETE" });
      if (!response.ok) {
        throw new Error(`Remove failed: ${response.status}`);
      }
    } catch {
      store.setState((state) => ({ ...state, lines: insertAt(state.lines, line, index), error: REMOVE_FAILED }));
    }
    finish();
  }, ms);

  function undo() {
    if (!open) {
      return;
    }
    open = false;
    clearTimeout(timer);
    store.setState((state) => ({ ...state, lines: insertAt(state.lines, line, index), undo: null }));
    finish();
  }

  return { undo, finished };
}
