// The typed client for Dhaaga's stock server.

/** What GET /api/stock/<id> sends back, for example { "id": "DC-1042", "stock": 3 }. */
export interface StockReply {
  id: string;
  stock: number;
}

/** What the stock badge shows: a count, or a message when the check failed. */
export type StockResult =
  | { ok: true; stock: number }
  | { ok: false; error: string };

/**
 * Asks the server for one item's stock, with fetch(`/api/stock/${id}`).
 * When the reply isn't ok, throws an Error "Stock check failed: 503" (with the real status).
 */
export async function fetchStock(id: string): Promise<StockReply> {
  const response = await fetch(`/api/stock/${id}`);
  if (!response.ok) {
    throw new Error(`Stock check failed: ${response.status}`);
  }
  return (await response.json()) as StockReply;
}

/**
 * Like fetchStock, but it never rejects.
 * Success gives { ok: true, stock }. Any failure gives { ok: false, error }, where error is the Error's
 * message, or the thrown value turned into text when it isn't an Error.
 */
export async function stockResult(id: string): Promise<StockResult> {
  try {
    const reply = await fetchStock(id);
    return { ok: true, stock: reply.stock };
  } catch (error) {
    return { ok: false, error: error instanceof Error ? error.message : String(error) };
  }
}

/** Asks for every id at the same time (all the requests start at once) and returns a Map from id to stock. */
export async function stockForAll(ids: string[]): Promise<Map<string, number>> {
  const replies = await Promise.all(ids.map((id) => fetchStock(id)));
  const stock = new Map<string, number>();
  for (const reply of replies) {
    stock.set(reply.id, reply.stock);
  }
  return stock;
}
