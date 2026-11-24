import { describe, it, expect, afterEach, vi } from "vitest";
import { stockUrl, getStock, isInStock } from "../src/stock.js";

/**
 * A pretend server. GET /api/stock/<id> answers 200 with { id, stock } for ids in the table,
 * 404 for anything else, and 500 for every request when broken is true.
 */
function pretendServer(table, { broken = false } = {}) {
  const fetchMock = vi.fn(async (url) => {
    if (broken) {
      return { ok: false, status: 500, json: async () => ({ error: "Server error" }) };
    }
    const id = String(url).split("/").pop();
    if (!(id in table)) {
      return { ok: false, status: 404, json: async () => ({ error: "No such item" }) };
    }
    return { ok: true, status: 200, json: async () => ({ id, stock: table[id] }) };
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

const table = { "DC-1042": 3, "DC-2001": 12, "DC-3001": 0 };

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("stockUrl", () => {
  it("builds the stock endpoint for an item", () => {
    expect(stockUrl("DC-1042"), "Golu's stock lives at /api/stock/DC-1042").toBe("/api/stock/DC-1042");
    expect(stockUrl("DC-2001"), "the URL should use the id it is given").toBe("/api/stock/DC-2001");
  });
});

describe("getStock", () => {
  it("asks the server once, at the item's stock URL", async () => {
    const fetchMock = pretendServer(table);
    await getStock("DC-1042");
    expect(fetchMock.mock.calls.length, "getStock should call fetch exactly once").toBe(1);
    expect(fetchMock.mock.calls[0][0], "fetch should be called with /api/stock/DC-1042").toBe("/api/stock/DC-1042");
  });

  it("gives back the stock number from the JSON body", async () => {
    pretendServer(table);
    expect(await getStock("DC-1042"), "the server says Golu (DC-1042) has 3 left").toBe(3);
    expect(await getStock("DC-2001"), "the server says DC-2001 has 12 left").toBe(12);
  });

  it("throws an error that names the status for an unknown item", async () => {
    pretendServer(table);
    let error = null;
    try {
      await getStock("DC-9999");
    } catch (e) {
      error = e;
    }
    expect(error, "a 404 answer isn't ok, so getStock should throw").not.toBeNull();
    expect(String(error?.message), "the error message should include the status 404").toContain("404");
  });

  it("throws an error that names the status when the server breaks", async () => {
    pretendServer(table, { broken: true });
    let error = null;
    try {
      await getStock("DC-1042");
    } catch (e) {
      error = e;
    }
    expect(error, "a 500 answer isn't ok, so getStock should throw").not.toBeNull();
    expect(String(error?.message), "the error message should include the status 500").toContain("500");
  });
});

describe("isInStock", () => {
  it("is true when at least one is left", async () => {
    pretendServer(table);
    expect(await isInStock("DC-1042"), "3 left means Golu is in stock").toBe(true);
  });

  it("is false when the stock is 0", async () => {
    pretendServer(table);
    expect(await isInStock("DC-3001"), "0 left means DC-3001 is not in stock").toBe(false);
  });
});
