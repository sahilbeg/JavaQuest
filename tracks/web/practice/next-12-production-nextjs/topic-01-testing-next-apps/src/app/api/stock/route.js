import { getItem } from "../../../lib/catalogue.js";

// GET /api/stock?id=DC-1042 answers { id, stock }.
export async function GET(request) {
  const id = new URL(request.url).searchParams.get("id");
  if (!id) {
    return Response.json({ error: "Missing id" }, { status: 400 });
  }
  const item = await getItem(id);
  if (!item) {
    return Response.json({ error: `No item ${id}` }, { status: 404 });
  }
  return Response.json({ id: item.id, stock: item.stock });
}
