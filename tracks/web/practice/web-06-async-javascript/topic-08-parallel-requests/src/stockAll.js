// Stock for every item on the stall's table, asked for all at once.

/** Already written: asks GET /api/stock/<id> and returns the number. Throws when the answer isn't ok. */
export async function getStock(id) {
  const response = await fetch(`/api/stock/${id}`);
  if (!response.ok) {
    throw new Error(`Stock check failed for ${id}: ${response.status}`);
  }
  const data = await response.json();
  return data.stock;
}

/**
 * Asks for every id at the same time and returns [{ id, stock }, ...] in the same order as ids.
 * If any request fails, the whole thing rejects.
 */
export async function stockForAll(ids) {
  // TODO 1: start every request at once with ids.map, and wait for them all with Promise.all.
  // TODO 2: pair each stock with its id by position: [{ id: "DC-1042", stock: 3 }, ...].
  return [];
}

/**
 * Asks for every id at the same time and never rejects. Returns
 *   { found: [{ id, stock }, ...], failed: [id, ...] }
 * with both lists in the same order as ids.
 */
export async function stockReport(ids) {
  // TODO 3: start every request at once and wait for them all with Promise.allSettled.
  // TODO 4: put each fulfilled result in found as { id, stock }, and each rejected id in failed.
  return { found: [], failed: [] };
}
