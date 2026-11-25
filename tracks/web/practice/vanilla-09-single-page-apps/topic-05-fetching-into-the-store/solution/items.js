// Loads the catalogue from GET /api/items into the store, once, and shares it with every screen.
// The server answers with a list such as [{ "id": "DC-1042", "name": "Golu the elephant", "paise": 45000 }].
// The store's items slice looks like { status: "idle" | "loading" | "ready" | "error", data: [...], error: null | text }.

export const FAILED = "Couldn't load the catalogue. Check your connection and try again.";

export const initialItems = { status: "idle", data: [], error: null };

/** Returns { load }. load() fetches the items into the store, unless they are already there. Never throws. */
export function createItemLoader(store) {
  let inFlight = null;

  async function request() {
    store.setState((state) => ({ ...state, items: { ...state.items, status: "loading", error: null } }));
    try {
      const response = await fetch("/api/items");
      if (!response.ok) {
        throw new Error(`Items failed: ${response.status}`);
      }
      const data = await response.json();
      store.setState((state) => ({ ...state, items: { status: "ready", data, error: null } }));
    } catch {
      store.setState((state) => ({ ...state, items: { ...state.items, status: "error", error: FAILED } }));
    }
  }

  function load() {
    if (store.getState().items.status === "ready") {
      return Promise.resolve();
    }
    if (inFlight === null) {
      inFlight = request().finally(() => {
        inFlight = null;
      });
    }
    return inFlight;
  }

  return { load };
}

/** A selector: the item with this id from the loaded items, or null. */
export function selectItem(state, id) {
  return state.items.data.find((item) => item.id === id) ?? null;
}
