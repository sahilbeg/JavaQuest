// Live stock badges for the catalogue. GET /api/stock/DC-1042 answers { "id": "DC-1042", "stock": 2 }.
// Each badge in the page looks like <span class="badge" data-item="DC-1042"></span>.

/** "Sold out" for 0, "Only 2 left" for 1 to 3 (with the real number), "In stock" for 4 or more. */
export function badgeText(stock) {
  if (stock === 0) return "Sold out";
  if (stock <= 3) return `Only ${stock} left`;
  return "In stock";
}

/** "out" for 0, "low" for 1 to 3, "ok" for 4 or more. */
export function badgeLevel(stock) {
  if (stock === 0) return "out";
  if (stock <= 3) return "low";
  return "ok";
}

/**
 * Asks for every id at the same time and returns an object such as { "DC-1042": 2, "DC-2001": 12 }.
 * Rejects if any answer isn't ok.
 */
export async function fetchStock(ids) {
  const stocks = await Promise.all(
    ids.map(async (id) => {
      const response = await fetch(`/api/stock/${id}`);
      if (!response.ok) {
        throw new Error(`Stock check failed for ${id}: ${response.status}`);
      }
      const data = await response.json();
      return data.stock;
    }),
  );
  const byId = {};
  ids.forEach((id, i) => {
    byId[id] = stocks[i];
  });
  return byId;
}

/** Sets the text and data-level of every [data-item] badge inside root whose id is in stockById. */
export function renderBadges(root, stockById) {
  for (const badge of root.querySelectorAll("[data-item]")) {
    const stock = stockById[badge.dataset.item];
    if (stock === undefined) continue;
    badge.textContent = badgeText(stock);
    badge.dataset.level = badgeLevel(stock);
  }
}

/**
 * Fills the badges inside root now, then again every ms milliseconds.
 * A failed refresh keeps the old badges. Returns a function that stops the refreshing.
 */
export function startLiveStock(root, ms = 30000) {
  const ids = [...root.querySelectorAll("[data-item]")].map((badge) => badge.dataset.item);

  async function refresh() {
    try {
      renderBadges(root, await fetchStock(ids));
    } catch (error) {
      console.error(error);
    }
  }

  refresh();
  const timer = setInterval(refresh, ms);
  return () => clearInterval(timer);
}
