import { describe, it, expect, beforeEach } from "vitest";
import { GET, POST } from "../src/app/api/stock/route.js";
import { resetStock, getLeft } from "../src/lib/stock.js";

beforeEach(() => {
  resetStock();
});

/** A POST to /api/stock with this body text. */
function post(text) {
  return new Request("http://localhost/api/stock", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: text,
  });
}

describe("GET /api/stock", () => {
  it("answers how many of an item are left", async () => {
    const res = await GET(new Request("http://localhost/api/stock?id=DC-1042"));
    expect(res.status, "a known item should answer status 200").toBe(200);
    expect(await res.json(), "Golu has 7 left, so the answer should be { id: \"DC-1042\", left: 7 }").toEqual({ id: "DC-1042", left: 7 });
  });

  it("answers 0 for a sold-out item, not 404", async () => {
    const res = await GET(new Request("http://localhost/api/stock?id=DC-2001"));
    expect(res.status, "the coaster set exists with 0 left, so the status should be 200 (check left === undefined, not !left)").toBe(200);
    expect(await res.json(), "the answer should be { id: \"DC-2001\", left: 0 }").toEqual({ id: "DC-2001", left: 0 });
  });

  it("answers 400 when the id is missing", async () => {
    const res = await GET(new Request("http://localhost/api/stock"));
    expect(res.status, "no ?id= in the URL should answer 400").toBe(400);
    expect(await res.json(), "the body should explain what to add").toEqual({ error: "Add ?id= with an item id." });
  });

  it("answers 404 for an item that doesn't exist", async () => {
    const res = await GET(new Request("http://localhost/api/stock?id=DC-9999"));
    expect(res.status, "an unknown item should answer 404").toBe(404);
    expect(await res.json(), "the body should name the item").toEqual({ error: "No item DC-9999." });
  });
});

describe("POST /api/stock", () => {
  it("adds stock and answers the new count", async () => {
    const res = await POST(post(JSON.stringify({ id: "DC-4410", add: 5 })));
    expect(res.status, "a good restock should answer 200").toBe(200);
    expect(await res.json(), "12 booties plus 5 makes 17").toEqual({ id: "DC-4410", left: 17 });
    expect(getLeft("DC-4410"), "the stock itself should now say 17 (use addStock)").toBe(17);
  });

  it("refuses a body that isn't JSON", async () => {
    const res = await POST(post("five booties please"));
    expect(res.status, "a body that isn't JSON should answer 400 (wrap request.json() in try/catch)").toBe(400);
    expect(await res.json(), "the body should say The body must be JSON.").toEqual({ error: "The body must be JSON." });
  });

  it("refuses an amount that isn't a whole number above 0, and changes nothing", async () => {
    for (const add of [-3, 0, 2.5, "5"]) {
      const res = await POST(post(JSON.stringify({ id: "DC-4410", add })));
      expect(res.status, `add: ${JSON.stringify(add)} should answer 400`).toBe(400);
      expect(await res.json(), `add: ${JSON.stringify(add)} should explain the rule`).toEqual({ error: "add must be a whole number above 0." });
    }
    expect(getLeft("DC-4410"), "refused requests must not change the stock").toBe(12);
  });

  it("answers 404 for an item that doesn't exist", async () => {
    const res = await POST(post(JSON.stringify({ id: "DC-9999", add: 5 })));
    expect(res.status, "restocking an unknown item should answer 404").toBe(404);
    expect(await res.json(), "the body should name the item").toEqual({ error: "No item DC-9999." });
  });
});
