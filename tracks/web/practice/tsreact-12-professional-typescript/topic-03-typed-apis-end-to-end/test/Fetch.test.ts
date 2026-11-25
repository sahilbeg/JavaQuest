import { describe, it, expect, afterEach, vi } from "vitest";
import { fetchItems } from "../src/api.ts";

afterEach(() => {
  vi.unstubAllGlobals();
});

/** A pretend server that answers every request with this status and JSON. */
function serverAnswers(status: number, body: unknown) {
  const fake = vi.fn(async (_url: string) => ({
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  }));
  vi.stubGlobal("fetch", fake);
  return fake;
}

describe("fetchItems", () => {
  it("asks the right url and gives the checked items", async () => {
    const fake = serverAnswers(200, [{ id: "DC-1042", name: "Golu the elephant", price_paise: 45000, stock: 7 }]);
    const result = await fetchItems("/api/items");
    expect(fake.mock.calls.map((call) => call[0]), "fetchItems should fetch the url it was given (TODO 3)").toEqual(["/api/items"]);
    expect(result, "a good answer should give { ok: true, value: [the Items] } (TODO 3)").toEqual({
      ok: true,
      value: [{ id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 7 }],
    });
  });

  it("turns an answer that isn't ok into an error", async () => {
    serverAnswers(503, { message: "busy" });
    expect(await fetchItems("/api/items"), "a 503 answer should give { ok: false, error: \"HTTP 503\" } (TODO 3)").toEqual({ ok: false, error: "HTTP 503" });
  });

  it("refuses data with the wrong shape instead of trusting it", async () => {
    serverAnswers(200, [{ id: "DC-1042", name: "Golu the elephant", price: "450", stock: 7 }]);
    expect(await fetchItems("/api/items"), "items without a whole-number price_paise should give the \"don't understand\" error (TODO 3)").toEqual({
      ok: false,
      error: "The server sent items we don't understand.",
    });
  });

  it("never throws when the network fails", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => {
        throw new TypeError("Failed to fetch");
      })
    );
    expect(await fetchItems("/api/items"), "a failed fetch should give { ok: false, error: \"Failed to fetch\" }, not throw (TODO 3)").toEqual({ ok: false, error: "Failed to fetch" });
  });

  it("never throws when the answer isn't JSON", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => ({
        ok: true,
        status: 200,
        json: async () => {
          throw new SyntaxError("Unexpected token '<'");
        },
      }))
    );
    const result = await fetchItems("/api/items");
    expect(result.ok, "an HTML error page instead of JSON should give ok: false, not throw (TODO 3)").toBe(false);
  });
});
