// Live stock badges for the catalogue. GET /api/stock/DC-1042 answers { "id": "DC-1042", "stock": 2 }.
// Each badge in the page looks like <span class="badge" data-item="DC-1042"></span>.

/** "Sold out" for 0, "Only 2 left" for 1 to 3 (with the real number), "In stock" for 4 or more. */
export function badgeText(stock) {
  // TODO 1: return the badge text for this stock number.
  return "";
}

/** "out" for 0, "low" for 1 to 3, "ok" for 4 or more. */
export function badgeLevel(stock) {
  // TODO 2: return the badge level for this stock number.
  return "";
}

/**
 * Asks for every id at the same time and returns an object such as { "DC-1042": 2, "DC-2001": 12 }.
 * Rejects if any answer isn't ok.
 */
export async function fetchStock(ids) {
  // TODO 3: fetch `/api/stock/${id}` for every id at once with Promise.all. Throw when an answer isn't ok.
  // TODO 4: build and return an object that maps each id to its stock.
  return {};
}

/** Sets the text and data-level of every [data-item] badge inside root whose id is in stockById. */
export function renderBadges(root, stockById) {
  // TODO 5: loop over root.querySelectorAll("[data-item]"). Skip ids missing from stockById.
  //         Set badge.textContent with badgeText and badge.dataset.level with badgeLevel.
}

/**
 * Fills the badges inside root now, then again every ms milliseconds.
 * A failed refresh keeps the old badges. Returns a function that stops the refreshing.
 */
export function startLiveStock(root, ms = 30000) {
  // TODO 6: read the ids from the badges' data-item attributes.
  // TODO 7: write an async refresh that renders fetchStock(ids), and keeps the old badges if it fails.
  // TODO 8: call refresh() once now, then every ms with setInterval. Return a function that calls clearInterval.
  return () => {};
}
