import { getLeft, addStock } from "../../../lib/stock.js";

/** GET /api/stock?id=DC-1042 */
export async function GET(request) {
  const id = new URL(request.url).searchParams.get("id");
  if (!id) return Response.json({ error: "Add ?id= with an item id." }, { status: 400 });

  const left = getLeft(id);
  if (left === undefined) return Response.json({ error: `No item ${id}.` }, { status: 404 });

  return Response.json({ id, left });
}

/** POST /api/stock with a JSON body like { "id": "DC-4410", "add": 5 } */
export async function POST(request) {
  let body;
  try {
    body = await request.json();
  } catch {
    return Response.json({ error: "The body must be JSON." }, { status: 400 });
  }

  const { id, add } = body ?? {};
  if (!Number.isInteger(add) || add <= 0) {
    return Response.json({ error: "add must be a whole number above 0." }, { status: 400 });
  }
  if (getLeft(id) === undefined) return Response.json({ error: `No item ${id}.` }, { status: 404 });

  return Response.json({ id, left: addStock(id, add) });
}
