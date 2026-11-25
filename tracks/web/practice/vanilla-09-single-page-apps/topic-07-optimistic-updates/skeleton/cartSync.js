// Cart changes that show at once and are saved in the background, with undo for removals.
// The state looks like { lines: [{ id: "DC-1042", qty: 1 }], undo: null | id, error: null | text }.

export const UNDO_MS = 5000;
export const SAVE_FAILED = "Couldn't update your cart. Please try again.";
export const REMOVE_FAILED = "Couldn't remove that item. It's back in your cart.";

/** A new array with line put back at index. The old array is not changed. */
export function insertAt(lines, line, index) {
  // TODO 1: return a new array: the lines before index, then line, then the rest (slice and spread work well).
  return [];
}

/** Shows the new qty at once, then saves it. If saving fails, puts the old qty back. Never throws. */
export async function changeQty(store, id, qty) {
  const before = store.getState().lines.find((line) => line.id === id);
  if (before === undefined) {
    return;
  }
  const setQty = (value) =>
    store.setState((state) => ({ ...state, lines: state.lines.map((line) => (line.id === id ? { ...line, qty: value } : line)) }));

  // TODO 2: show the new qty straight away with setQty(qty), before asking the server.

  // TODO 3: in try...catch, send fetch(`/api/cart/${id}`, { method: "PATCH", headers: { "Content-Type": "application/json" },
  //         body: JSON.stringify({ qty }) }) and throw when response.ok is false.
  //         In catch, put this line's old qty back with setQty(before.qty) and set the state's error to SAVE_FAILED.
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

  // TODO 4: take the line out of the cart now (filter), and set the state's undo to id, so the page shows "Undo".

  const timer = setTimeout(async () => {
    open = false;
    // TODO 5: set undo back to null. Then, in try...catch, send fetch(`/api/cart/${id}`, { method: "DELETE" })
    //         and throw when response.ok is false. In catch, put the line back with insertAt(state.lines, line, index)
    //         and set error to REMOVE_FAILED.
    finish();
  }, ms);

  function undo() {
    if (!open) {
      return;
    }
    open = false;
    clearTimeout(timer);
    // TODO 6: put the line back at its old index with insertAt, and set undo back to null. Send nothing to the server.
    finish();
  }

  return { undo, finished };
}
