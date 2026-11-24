import { vi } from "vitest";

/** The stock API's answers in these checks. The stock stays still, unlike during the sale. */
export const STOCK = {
  "/api/stock/DC-1042": { id: "DC-1042", left: 7 },
  "/api/stock/DC-2001": { id: "DC-2001", left: 3 },
};

/** A pretend fetch response: ok for 2xx codes, with the data as its JSON. */
export function reply(data, status = 200) {
  return { ok: status >= 200 && status < 300, status, json: async () => data };
}

/** Replaces the global fetch with a mock that answers with handler(url). Returns the mock. */
export function stubFetch(handler) {
  const fetchMock = vi.fn(handler);
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** A pretend stock API that answers straight away. */
export function stockApi() {
  return stubFetch(async (url) => reply(STOCK[url]));
}

/** A fetch whose answers wait until the test releases them with waiting[url](response). */
export function heldFetch() {
  const waiting = {};
  const fetchMock = stubFetch((url) => new Promise((resolve) => (waiting[url] = resolve)));
  return { fetchMock, waiting };
}

/** Gives promises and timers a moment to finish, so an extra request or a late update would show up. */
export function settle() {
  return new Promise((resolve) => setTimeout(resolve, 20));
}
