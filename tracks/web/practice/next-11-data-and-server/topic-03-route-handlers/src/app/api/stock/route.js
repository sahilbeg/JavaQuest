import { getLeft, addStock } from "../../../lib/stock.js";

/** GET /api/stock?id=DC-1042 */
export async function GET(request) {
  // TODO 1: read id with new URL(request.url).searchParams.get("id"),
  // and answer Response.json({ id, left }) with the number from getLeft(id).
  // TODO 2: no id: 400 { error: "Add ?id= with an item id." }. Unknown item: 404 { error: `No item ${id}.` }.
  return Response.json({ error: "Not built yet." }, { status: 501 });
}

/** POST /api/stock with a JSON body like { "id": "DC-4410", "add": 5 } */
export async function POST(request) {
  // TODO 3: read the body with await request.json(), add the stock with addStock(id, add),
  // and answer Response.json({ id, left }) with the new number.
  // TODO 4: check first. Bad JSON: 400 { error: "The body must be JSON." }.
  // add not a whole number above 0: 400 { error: "add must be a whole number above 0." }.
  // Unknown item: 404 { error: `No item ${id}.` }.
  return Response.json({ error: "Not built yet." }, { status: 501 });
}
