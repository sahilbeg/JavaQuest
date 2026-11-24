import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { stockReport } from "../src/stockAll.js";
import { slowServer, track } from "./slowServer.js";

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe("stockReport", () => {
  it("lists every item as found when all requests work", async () => {
    slowServer();
    const result = track(stockReport(["DC-1042", "DC-2001", "DC-3001"]));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.value, "all three items should be in found, with nothing in failed").toEqual({
      found: [
        { id: "DC-1042", stock: 3 },
        { id: "DC-2001", stock: 12 },
        { id: "DC-3001", stock: 0 },
      ],
      failed: [],
    });
  });

  it("keeps the good answers when one item fails", async () => {
    slowServer();
    const result = track(stockReport(["DC-1042", "DC-9999", "DC-2001"]));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.error, "stockReport should never reject; use Promise.allSettled").toBeNull();
    expect(result.value, "DC-9999 should be in failed, and the other two in found").toEqual({
      found: [
        { id: "DC-1042", stock: 3 },
        { id: "DC-2001", stock: 12 },
      ],
      failed: ["DC-9999"],
    });
  });

  it("lists failed ids in the order they were asked", async () => {
    slowServer();
    const result = track(stockReport(["DC-9999", "DC-1042", "DC-0000"]));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.value?.failed, "both unknown ids should be in failed, in the order of ids").toEqual(["DC-9999", "DC-0000"]);
    expect(result.value?.found, "DC-1042 should still be found").toEqual([{ id: "DC-1042", stock: 3 }]);
  });

  it("asks for every item at once", async () => {
    const fetchMock = slowServer();
    const result = track(stockReport(["DC-1042", "DC-2001", "DC-3001"]));
    await vi.advanceTimersByTimeAsync(0);
    expect(fetchMock.mock.calls.length, "all 3 requests should start before any answer arrives").toBe(3);
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.done, "three 1-second requests in parallel should be done after 1 second").toBe(true);
  });
});
