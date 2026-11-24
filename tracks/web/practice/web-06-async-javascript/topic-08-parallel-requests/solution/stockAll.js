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
  const stocks = await Promise.all(ids.map((id) => getStock(id)));
  return ids.map((id, i) => ({ id, stock: stocks[i] }));
}

/**
 * Asks for every id at the same time and never rejects. Returns
 *   { found: [{ id, stock }, ...], failed: [id, ...] }
 * with both lists in the same order as ids.
 */
export async function stockReport(ids) {
  const results = await Promise.allSettled(ids.map((id) => getStock(id)));
  const report = { found: [], failed: [] };
  results.forEach((result, i) => {
    if (result.status === "fulfilled") {
      report.found.push({ id: ids[i], stock: result.value });
    } else {
      report.failed.push(ids[i]);
    }
  });
  return report;
}
