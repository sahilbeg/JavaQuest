const API = "https://api.dhaaga.in";

/** Fetches a path from the Dhaaga API with the given fetch options, and returns its JSON. */
async function getJson(path, options) {
  const res = await fetch(`${API}${path}`, options);
  if (!res.ok) throw new Error(`Dhaaga API answered ${res.status} for ${path}`);
  return res.json();
}

/** Items and prices. They change a few times a month. */
export function getCatalogue() {
  return getJson("/items", { next: { revalidate: 3600, tags: ["catalogue"] } });
}

/** Stock counts. They change with every sale. */
export function getStock() {
  return getJson("/stock", { cache: "no-store" });
}

/** Opening hours of the stall and the shop. They change a few times a year. */
export function getShopHours() {
  return getJson("/hours", { cache: "force-cache", next: { tags: ["hours"] } });
}
