import { describe, it, expect, vi } from "vitest";
import { totalStock } from "../src/order.js";

const shelf = { "DC-1042": 3, "DC-2001": 12, "DC-3001": 0 };

describe("totalStock", () => {
  it("adds up the stock of every item", async () => {
    const getStock = vi.fn(async (id) => shelf[id]);
    const total = await totalStock(getStock, ["DC-1042", "DC-2001", "DC-3001"]);
    expect(total, "3 + 12 + 0 should make 15; did you await getStock(id)?").toBe(15);
  });

  it("asks once for each item", async () => {
    const getStock = vi.fn(async (id) => shelf[id]);
    await totalStock(getStock, ["DC-1042", "DC-2001"]);
    expect(getStock.mock.calls, "getStock should be called once per id, in order").toEqual([["DC-1042"], ["DC-2001"]]);
  });

  it("waits for each answer before asking for the next", async () => {
    const log = [];
    const getStock = vi.fn(async (id) => {
      log.push(`ask ${id}`);
      await Promise.resolve();
      log.push(`got ${id}`);
      return 1;
    });
    await totalStock(getStock, ["DC-1042", "DC-2001"]);
    expect(log, "use for...of with await, so each answer arrives before the next question").toEqual([
      "ask DC-1042",
      "got DC-1042",
      "ask DC-2001",
      "got DC-2001",
    ]);
  });

  it("gives 0 for an empty list", async () => {
    const getStock = vi.fn(async (id) => shelf[id]);
    const total = await totalStock(getStock, []);
    expect(total, "no items means a total of 0").toBe(0);
  });
});
