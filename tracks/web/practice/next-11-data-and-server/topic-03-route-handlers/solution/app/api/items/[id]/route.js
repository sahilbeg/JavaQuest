import { findItem } from "../../../../lib/catalogue.js";

/** GET /api/items/DC-1042 */
export async function GET(request, { params }) {
  const { id } = await params;
  const item = findItem(id);
  if (!item) return Response.json({ error: `No item ${id}.` }, { status: 404 });
  return Response.json(item);
}
