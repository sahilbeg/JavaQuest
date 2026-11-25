import { listItems } from "../../../lib/catalogue.js";

// Round 1, the API round: "Write GET /api/search?q=<words>&max=<paise>."
// The candidate's answer. It worked when they tried ?q=Golu&max=50000, once.
export async function GET(request) {
  // TODO 1: fix the five bugs the README lists: q must ignore case and spaces, a missing max means no limit,
  // a max that isn't a whole number answers 400, results go cheapest first, and each result is only { id, name, paise }.
  const params = new URL(request.url).searchParams;
  const q = params.get("q") ?? "";
  const max = Number(params.get("max"));
  const items = await listItems();
  const results = items.filter((item) => item.name.includes(q) && item.paise <= max);
  return Response.json({ results });
}
