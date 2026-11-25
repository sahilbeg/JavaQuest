const API = "https://api.dhaaga.in";

/** Fetches a path from the Dhaaga API with the given fetch options, and returns its JSON. */
async function getJson(path, options) {
  const res = await fetch(`${API}${path}`, options);
  if (!res.ok) throw new Error(`Dhaaga API answered ${res.status} for ${path}`);
  return res.json();
}

/** Items and prices. They change a few times a month. */
export function getCatalogue() {
  // TODO 1: cache the catalogue for an hour at most (3600 seconds), and tag it "catalogue".
  return getJson("/items");
}

/** Stock counts. They change with every sale. */
export function getStock() {
  // TODO 2: stock must be true right now. This option keeps a copy all day: never cache stock.
  return getJson("/stock", { cache: "force-cache" });
}

/** Opening hours of the stall and the shop. They change a few times a year. */
export function getShopHours() {
  // TODO 3: keep a copy until told otherwise, and tag it "hours".
  return getJson("/hours");
}
