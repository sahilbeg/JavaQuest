/** How many times sortByPrice has run, so the checks (and the preview) can count sorts. */
export const sortStats = { runs: 0 };

/** The items sorted by price: "low-first" or "high-first". Sorts a copy, never the array it is given. */
export function sortByPrice(items, order) {
  sortStats.runs += 1;
  const sorted = [...items].sort((a, b) => a.paise - b.paise);
  return order === "high-first" ? sorted.reverse() : sorted;
}
