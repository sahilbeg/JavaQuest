import { describe, it, expect } from "vitest";
import { GET } from "../src/app/api/search/route.js";

/** Calls GET /api/search with a query string like "?q=golu". */
async function search(query = "") {
  const response = await GET(new Request("http://localhost/api/search" + query));
  return { status: response.status, body: await response.json() };
}

function ids(answer) {
  return (answer.body.results ?? []).map((item) => item.id);
}

describe("Round 1: GET /api/search", () => {
  it("lists every item, cheapest first, when there is no q and no max", async () => {
    const answer = await search();
    expect(answer.status, "a search with no filters should answer 200").toBe(200);
    expect(ids(answer), "no max means no price limit, and results go cheapest first: keychain, coasters, booties, Golu, tote (TODO 1)").toEqual([
      "DC-1205",
      "DC-1101",
      "DC-1402",
      "DC-1042",
      "DC-1301",
    ]);
  });

  it("finds Golu whatever case the customer types", async () => {
    expect(ids(await search("?q=golu")), "\"golu\" should find Golu the elephant: ignore upper and lower case (TODO 1)").toEqual(["DC-1042"]);
    expect(ids(await search("?q=TOTE")), "\"TOTE\" should find the Tote bag (TODO 1)").toEqual(["DC-1301"]);
  });

  it("ignores spaces around the search words", async () => {
    expect(ids(await search("?q=%20%20coaster%20")), "\"  coaster \" should find the coaster set: trim the spaces (TODO 1)").toEqual(["DC-1101"]);
  });

  it("keeps items at or below max, including an item that costs exactly max", async () => {
    expect(ids(await search("?max=40000")), "max=40000 should keep the keychain, coasters and booties, cheapest first (TODO 1)").toEqual(["DC-1205", "DC-1101", "DC-1402"]);
    expect(ids(await search("?max=45000")), "Golu costs exactly 45000, so max=45000 keeps him (TODO 1)").toEqual(["DC-1205", "DC-1101", "DC-1402", "DC-1042"]);
  });

  it("answers 400 when max is not a whole number of paise", async () => {
    for (const max of ["abc", "12.5", "-1", ""]) {
      expect(await search("?max=" + max), `max=${max} should answer 400 with an error (TODO 1)`).toEqual({
        status: 400,
        body: { error: "max must be a whole number of paise" },
      });
    }
  });

  it("sends only id, name and paise, never stock or our cost", async () => {
    const answer = await search("?q=golu");
    expect(answer.body.results, "each result should be exactly { id, name, paise } (TODO 1)").toEqual([{ id: "DC-1042", name: "Golu the elephant", paise: 45000 }]);
  });
});
