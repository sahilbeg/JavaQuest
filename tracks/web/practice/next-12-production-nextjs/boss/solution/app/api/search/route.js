import { listItems } from "../../../lib/catalogue.js";

// Round 1, the API round: "Write GET /api/search?q=<words>&max=<paise>."
export async function GET(request) {
  const params = new URL(request.url).searchParams;
  const q = (params.get("q") ?? "").trim().toLowerCase();
  const maxText = params.get("max");
  if (maxText !== null && !/^\d+$/.test(maxText)) {
    return Response.json({ error: "max must be a whole number of paise" }, { status: 400 });
  }
  const max = maxText === null ? Infinity : Number(maxText);
  const items = await listItems();
  const results = items
    .filter((item) => item.name.toLowerCase().includes(q) && item.paise <= max)
    .sort((a, b) => a.paise - b.paise)
    .map(({ id, name, paise }) => ({ id, name, paise }));
  return Response.json({ results });
}
