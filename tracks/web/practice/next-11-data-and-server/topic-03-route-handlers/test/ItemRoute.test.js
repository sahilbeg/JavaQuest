import { describe, it, expect } from "vitest";
import { GET } from "../src/app/api/items/[id]/route.js";

/** What Next.js passes as the second argument: params is a Promise in Next.js 16. */
function context(id) {
  return { params: Promise.resolve({ id }) };
}

describe("GET /api/items/[id]", () => {
  it("answers the item for a known id", async () => {
    const res = await GET(new Request("http://localhost/api/items/DC-1042"), context("DC-1042"));
    expect(res.status, "a known item should answer status 200").toBe(200);
    expect(await res.json(), "the answer should be Golu's item object").toEqual({ id: "DC-1042", name: "Golu the elephant", paise: 45000 });
  });

  it("uses the id from the URL", async () => {
    const res = await GET(new Request("http://localhost/api/items/DC-3007"), context("DC-3007"));
    expect((await res.json()).name, "DC-3007 is the Marigold tote bag (remember to await params)").toBe("Marigold tote bag");
  });

  it("answers 404 for an item that doesn't exist", async () => {
    const res = await GET(new Request("http://localhost/api/items/DC-9999"), context("DC-9999"));
    expect(res.status, "an unknown item should answer 404").toBe(404);
    expect(await res.json(), "the body should name the item").toEqual({ error: "No item DC-9999." });
  });
});
