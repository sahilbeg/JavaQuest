import { findItem } from "../../../../lib/catalogue.js";

/** GET /api/items/DC-1042 */
export async function GET(request, { params }) {
  // TODO 5: read the id with const { id } = await params; (params is a Promise in Next.js 16).
  // Answer the item from findItem(id), or 404 { error: `No item ${id}.` }.
  return Response.json({ error: "Not built yet." }, { status: 501 });
}
