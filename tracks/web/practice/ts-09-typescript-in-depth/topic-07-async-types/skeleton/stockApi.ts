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
  // TODO 1: await fetch, check response.ok, and return the JSON as a StockReply.
  return { id, stock: 0 };
}

/**
 * Like fetchStock, but it never rejects.
 * Success gives { ok: true, stock }. Any failure gives { ok: false, error }, where error is the Error's
 * message, or the thrown value turned into text when it isn't an Error.
 */
export async function stockResult(id: string): Promise<StockResult> {
  // TODO 2: try fetchStock; in catch, the error is unknown, so check instanceof Error before reading message.
  return { ok: false, error: "" };
}

/** Asks for every id at the same time (all the requests start at once) and returns a Map from id to stock. */
export async function stockForAll(ids: string[]): Promise<Map<string, number>> {
  // TODO 3: start every fetchStock call, wait for them all with Promise.all, then fill a Map.
  return new Map<string, number>();
}
