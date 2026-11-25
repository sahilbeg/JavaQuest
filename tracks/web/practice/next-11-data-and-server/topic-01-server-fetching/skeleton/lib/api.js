const API = "https://api.dhaaga.in";

/** The catalogue: a list like [{ id: "DC-1042", name: "Golu the elephant", paise: 45000 }]. */
export async function getItems() {
  // TODO 1: fetch `${API}/items`. If the answer is not ok, throw new Error(`Items API answered ${res.status}`).
  // Otherwise return its JSON.
  return [];
}

/** Stock counts by item id, like { "DC-1042": 7, "DC-2001": 0 }. */
export async function getStock() {
  // TODO 2: the same for `${API}/stock`, throwing new Error(`Stock API answered ${res.status}`) when it fails.
  return {};
}
