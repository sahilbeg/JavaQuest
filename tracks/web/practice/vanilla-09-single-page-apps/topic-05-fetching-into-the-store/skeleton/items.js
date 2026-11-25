// Loads the catalogue from GET /api/items into the store, once, and shares it with every screen.
// The server answers with a list such as [{ "id": "DC-1042", "name": "Golu the elephant", "paise": 45000 }].
// The store's items slice looks like { status: "idle" | "loading" | "ready" | "error", data: [...], error: null | text }.

export const FAILED = "Couldn't load the catalogue. Check your connection and try again.";

export const initialItems = { status: "idle", data: [], error: null };

/** Returns { load }. load() fetches the items into the store, unless they are already there. Never throws. */
export function createItemLoader(store) {
  let inFlight = null;

  async function request() {
    // TODO 1: set the items slice to status "loading" and error null, keeping its data:
    //         store.setState((state) => ({ ...state, items: { ...state.items, ... } })).

    // TODO 2: in try...catch, fetch "/api/items" and throw an Error when response.ok is false.
    //         On success set items to { status: "ready", data, error: null }.
    //         In catch set status "error" and error FAILED, and keep the old data.
  }

  function load() {
    // TODO 3: when items.status is already "ready", return Promise.resolve() without fetching.

    // TODO 4: when no request is in flight, start one and forget it once it settles:
    //         inFlight = request().finally(() => { inFlight = null; });
    //         Then return inFlight, so two screens asking at once share one request.
    return Promise.resolve();
  }

  return { load };
}

/** A selector: the item with this id from the loaded items, or null. */
export function selectItem(state, id) {
  // TODO 5: find the item with this id in state.items.data, or return null when there is none.
  return null;
}
