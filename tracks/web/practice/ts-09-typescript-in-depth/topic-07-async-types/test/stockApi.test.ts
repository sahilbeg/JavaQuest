import { describe, it, expect, vi, afterEach } from "vitest";
import { fetchStock, stockResult, stockForAll } from "../src/stockApi.ts";

/** A pretend fetch reply. */
function reply(status: number, body: unknown) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

/** A pretend stock server that knows these items. */
const STOCK: Record<string, number> = { "DC-1042": 3, "DC-1043": 0, "DC-1044": 8 };

function stockServer() {
  return vi.fn(async (url: string) => {
    const id = url.split("/").pop() ?? "";
    return id in STOCK ? reply(200, { id, stock: STOCK[id] }) : reply(404, { error: "not found" });
  });
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("fetchStock", () => {
  it("asks the stock server for the item and gives back its reply", async () => {
    const server = stockServer();
    vi.stubGlobal("fetch", server);
    const result = await fetchStock("DC-1042");
    expect(server.mock.calls[0]?.[0], "fetchStock should ask for /api/stock/DC-1042").toBe("/api/stock/DC-1042");
    expect(result, "the server says Golu has 3 in stock").toEqual({ id: "DC-1042", stock: 3 });
  });

  it("rejects with the status when the server says no", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => reply(503, { error: "busy" })));
    await expect(fetchStock("DC-1042"), "a 503 reply should reject with \"Stock check failed: 503\"").rejects.toThrow("Stock check failed: 503");
  });
});

describe("stockResult", () => {
  it("gives ok and the stock when the check works", async () => {
    vi.stubGlobal("fetch", stockServer());
    expect(await stockResult("DC-1044"), "8 keychains are in stock").toEqual({ ok: true, stock: 8 });
  });

  it("gives the error message when the server fails", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => reply(503, { error: "busy" })));
    expect(await stockResult("DC-1042"), "a failed check should become a message, not a rejection").toEqual({ ok: false, error: "Stock check failed: 503" });
  });

  it("gives the error message when the network is down", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => {
      throw new TypeError("Failed to fetch");
    }));
    expect(await stockResult("DC-1042"), "a network error's message should be shown").toEqual({ ok: false, error: "Failed to fetch" });
  });

  it("turns a thrown string into the message", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => {
      throw "offline";
    }));
    expect(await stockResult("DC-1042"), "something that isn't an Error should be turned into text").toEqual({ ok: false, error: "offline" });
  });
});

describe("stockForAll", () => {
  it("gives a Map from each id to its stock", async () => {
    vi.stubGlobal("fetch", stockServer());
    const stock = await stockForAll(["DC-1042", "DC-1043", "DC-1044"]);
    expect(stock.get("DC-1042"), "Golu has 3 in stock").toBe(3);
    expect(stock.get("DC-1043"), "the coaster set has 0 in stock").toBe(0);
    expect(stock.get("DC-1044"), "the keychain has 8 in stock").toBe(8);
  });

  it("starts every request at once instead of one after another", async () => {
    const waiting: Array<() => void> = [];
    const server = vi.fn((url: string) => new Promise((resolve) => {
      const id = url.split("/").pop() ?? "";
      waiting.push(() => resolve(reply(200, { id, stock: 1 })));
    }));
    vi.stubGlobal("fetch", server);
    const pending = stockForAll(["DC-1042", "DC-1043"]);
    expect(server.mock.calls.length, "both requests should start before either answer arrives (use Promise.all)").toBe(2);
    waiting.forEach((answer) => answer());
    const stock = await pending;
    expect(stock.size, "both items should be in the Map").toBe(2);
  });

  it("rejects when any check fails", async () => {
    vi.stubGlobal("fetch", stockServer());
    await expect(stockForAll(["DC-1042", "DC-9999"]), "an unknown item should make the whole list reject").rejects.toThrow("Stock check failed: 404");
  });
});
