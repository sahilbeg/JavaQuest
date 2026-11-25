import { getItem } from "../../../lib/store.js";

// GET /api/stock?id=DC-1042, for the delivery partner's app.
export async function GET(request) {
  // TODO 5: no id gives 400 { error: "Missing id" }; an unknown id gives 404 { error: "No item <id>" };
  // otherwise 200 { id, stock } with the store's live stock.
  return Response.json({ error: "Not built yet" }, { status: 501 });
}
