import { describe, it, expect, afterEach, vi } from "vitest";
import { createDeliveryChecker } from "../src/delivery.js";

/**
 * A pretend delivery API that answers only when the test says so, with answer(pin).
 * That lets a test decide which answer arrives first. Pune PINs (411...) are deliverable in 2 days.
 */
function controllableServer() {
  const waiting = new Map();
  const fetchMock = vi.fn(
    (url) =>
      new Promise((resolve) => {
        const pin = new URL(String(url), "https://dhaaga.in").searchParams.get("pin");
        waiting.set(pin, resolve);
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return {
    fetchMock,
    answer(pin) {
      const body = pin.startsWith("411") ? { deliverable: true, days: 2 } : { deliverable: false };
      waiting.get(pin)?.({ ok: true, status: 200, json: async () => body });
    },
  };
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("createDeliveryChecker", () => {
  it("shows the answer for a single check", async () => {
    const server = controllableServer();
    const show = vi.fn();
    const check = createDeliveryChecker(show);
    const done = check("411045");
    server.answer("411045");
    await done;
    expect(show.mock.calls, "one check should show one answer").toEqual([["Delivery to 411045 in 2 days"]]);
  });

  it("asks about the PIN it is given", async () => {
    const server = controllableServer();
    const check = createDeliveryChecker(vi.fn());
    const done = check("411045");
    server.answer("411045");
    await done;
    expect(server.fetchMock.mock.calls[0]?.[0], "fetch should be called with /api/delivery?pin=411045").toBe("/api/delivery?pin=411045");
  });

  it("ignores an old answer that arrives after the newest one", async () => {
    const server = controllableServer();
    const show = vi.fn();
    const check = createDeliveryChecker(show);
    const typo = check("560001");
    const fixed = check("411045");
    server.answer("411045");
    await fixed;
    server.answer("560001");
    await typo;
    expect(show.mock.calls, "only the newest check (411045) may change the screen; the late answer for 560001 must be ignored").toEqual([
      ["Delivery to 411045 in 2 days"],
    ]);
  });

  it("ignores an old answer even when it arrives first", async () => {
    const server = controllableServer();
    const show = vi.fn();
    const check = createDeliveryChecker(show);
    const typo = check("560001");
    const fixed = check("411045");
    server.answer("560001");
    await typo;
    expect(show.mock.calls.length, "the answer for 560001 is already out of date, because a newer check has started").toBe(0);
    server.answer("411045");
    await fixed;
    expect(show.mock.calls, "only the newest answer should be shown").toEqual([["Delivery to 411045 in 2 days"]]);
  });

  it("still shows every answer when the checks don't overlap", async () => {
    const server = controllableServer();
    const show = vi.fn();
    const check = createDeliveryChecker(show);
    const first = check("560001");
    server.answer("560001");
    await first;
    const second = check("411045");
    server.answer("411045");
    await second;
    expect(show.mock.calls, "a check that finished before the next one began is still the newest, so both should show").toEqual([
      ["Sorry, we don't deliver to 560001 yet"],
      ["Delivery to 411045 in 2 days"],
    ]);
  });

  it("keeps a separate count for each checker", async () => {
    const server = controllableServer();
    const showA = vi.fn();
    const showB = vi.fn();
    const checkA = createDeliveryChecker(showA);
    const checkB = createDeliveryChecker(showB);
    const a = checkA("411045");
    const b = checkB("560001");
    server.answer("411045");
    server.answer("560001");
    await Promise.all([a, b]);
    expect(showA.mock.calls.length, "checker A's only check is its newest, so it should show").toBe(1);
    expect(showB.mock.calls.length, "checker B's only check is its newest, so it should show").toBe(1);
  });
});
