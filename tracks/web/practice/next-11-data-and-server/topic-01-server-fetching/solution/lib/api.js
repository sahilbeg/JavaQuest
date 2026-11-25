const API = "https://api.dhaaga.in";

/** The catalogue: a list like [{ id: "DC-1042", name: "Golu the elephant", paise: 45000 }]. */
export async function getItems() {
  const res = await fetch(`${API}/items`);
  if (!res.ok) throw new Error(`Items API answered ${res.status}`);
  return res.json();
}

/** Stock counts by item id, like { "DC-1042": 7, "DC-2001": 0 }. */
export async function getStock() {
  const res = await fetch(`${API}/stock`);
  if (!res.ok) throw new Error(`Stock API answered ${res.status}`);
  return res.json();
}
