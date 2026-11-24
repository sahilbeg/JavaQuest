import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { startLiveStock } from "../src/badges.js";

let root;
let shelf;
let serverDown;
let fetchMock;

beforeEach(() => {
  vi.useFakeTimers();
  vi.spyOn(console, "error").mockImplementation(() => {});
  document.body.innerHTML = `
    <ul id="catalogue">
      <li>Golu the elephant <span class="badge" data-item="DC-1042"></span></li>
      <li>Marigold coaster set <span class="badge" data-item="DC-2001"></span></li>
      <li>Indigo tote bag <span class="badge" data-item="DC-3001"></span></li>
    </ul>
  `;
  root = document.querySelector("#catalogue");
  shelf = { "DC-1042": 2, "DC-2001": 12, "DC-3001": 0 };
  serverDown = false;
  fetchMock = vi.fn(async (url) => {
    if (serverDown) throw new TypeError("Failed to fetch");
    const id = String(url).split("/").pop();
    return { ok: true, status: 200, json: async () => ({ id, stock: shelf[id] }) };
  });
  vi.stubGlobal("fetch", fetchMock);
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

const texts = () => [...root.querySelectorAll("[data-item]")].map((badge) => badge.textContent);

describe("startLiveStock", () => {
  it("fills the badges straight away", async () => {
    startLiveStock(root);
    await vi.advanceTimersByTimeAsync(0);
    expect(fetchMock.mock.calls.length, "the first refresh should ask for all 3 items at once, without waiting 30 seconds").toBe(3);
    expect(texts(), "the badges should be filled from the first answers").toEqual(["Only 2 left", "In stock", "Sold out"]);
  });

  it("refreshes every 30 seconds", async () => {
    startLiveStock(root);
    await vi.advanceTimersByTimeAsync(0);
    shelf["DC-1042"] = 0;
    await vi.advanceTimersByTimeAsync(29999);
    expect(texts()[0], "nothing should change before 30 seconds").toBe("Only 2 left");
    await vi.advanceTimersByTimeAsync(1);
    expect(texts()[0], "after 30 seconds, Golu's badge should show the new stock").toBe("Sold out");
    expect(fetchMock.mock.calls.length, "two refreshes of 3 items make 6 requests").toBe(6);
  });

  it("uses the interval it is given", async () => {
    startLiveStock(root, 10000);
    await vi.advanceTimersByTimeAsync(0);
    shelf["DC-2001"] = 3;
    await vi.advanceTimersByTimeAsync(10000);
    expect(texts()[1], "with ms = 10000, the badges should refresh after 10 seconds").toBe("Only 3 left");
  });

  it("stops refreshing when stop() is called", async () => {
    const stop = startLiveStock(root);
    await vi.advanceTimersByTimeAsync(0);
    stop();
    shelf["DC-1042"] = 0;
    await vi.advanceTimersByTimeAsync(90000);
    expect(fetchMock.mock.calls.length, "after stop(), no more requests should go out").toBe(3);
    expect(texts()[0], "after stop(), the badges should stay as they were").toBe("Only 2 left");
  });

  it("keeps the last good badges when a refresh fails, and recovers on the next one", async () => {
    startLiveStock(root);
    await vi.advanceTimersByTimeAsync(0);
    serverDown = true;
    await vi.advanceTimersByTimeAsync(30000);
    expect(texts(), "a failed refresh should leave the badges as they were").toEqual(["Only 2 left", "In stock", "Sold out"]);
    serverDown = false;
    shelf["DC-3001"] = 5;
    await vi.advanceTimersByTimeAsync(30000);
    expect(texts()[2], "the next refresh should work again and update the tote bag").toBe("In stock");
  });
});
