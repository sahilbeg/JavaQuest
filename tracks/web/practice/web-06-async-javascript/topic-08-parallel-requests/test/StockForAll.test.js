import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { stockForAll } from "../src/stockAll.js";
import { slowServer, track } from "./slowServer.js";

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

const ids = ["DC-1042", "DC-2001", "DC-3001"];

describe("stockForAll", () => {
  it("starts every request straight away", async () => {
    const fetchMock = slowServer();
    track(stockForAll(ids));
    await vi.advanceTimersByTimeAsync(0);
    expect(fetchMock.mock.calls.length, "all 3 requests should start before any answer arrives; use ids.map, not await in a loop").toBe(3);
  });

  it("has every answer after one second, because they wait together", async () => {
    slowServer();
    const result = track(stockForAll(ids));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.done, "three 1-second requests in parallel should all be done after 1 second").toBe(true);
    expect(result.value, "each id should be paired with its stock").toEqual([
      { id: "DC-1042", stock: 3 },
      { id: "DC-2001", stock: 12 },
      { id: "DC-3001", stock: 0 },
    ]);
  });

  it("keeps the order of the ids, even when answers arrive out of order", async () => {
    slowServer({ "DC-1042": 300, "DC-2001": 100, "DC-3001": 200 });
    const result = track(stockForAll(ids));
    await vi.advanceTimersByTimeAsync(300);
    expect(result.value, "DC-2001 answers first, but the list should still follow the order of ids").toEqual([
      { id: "DC-1042", stock: 3 },
      { id: "DC-2001", stock: 12 },
      { id: "DC-3001", stock: 0 },
    ]);
  });

  it("rejects when any item fails", async () => {
    slowServer();
    const result = track(stockForAll(["DC-1042", "DC-9999"]));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.error, "a 404 for DC-9999 should reject the whole list, like Promise.all does").not.toBeNull();
    expect(String(result.error?.message), "the error should come from getStock and name DC-9999").toContain("DC-9999");
  });

  it("gives an empty list for no ids", async () => {
    slowServer();
    const result = track(stockForAll([]));
    await vi.advanceTimersByTimeAsync(0);
    expect(result.value, "no ids means an empty list").toEqual([]);
  });
});
