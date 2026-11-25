import { vi } from "vitest";

export const ITEMS_URL = "https://api.dhaaga.in/items";
export const STOCK_URL = "https://api.dhaaga.in/stock";

export const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-2001", name: "Coaster set", paise: 15000 },
];
export const STOCK = { "DC-1042": 7, "DC-2001": 0 };

/** A response like fetch gives back. */
export function reply(data, status = 200) {
  return { ok: status >= 200 && status < 300, status, json: async () => data };
}

/** A pretend Dhaaga API. Pass { url: [data, status] } to change an answer. Returns the fetch mock. */
export function stubApi(answers = {}) {
  const routes = { [ITEMS_URL]: [ITEMS, 200], [STOCK_URL]: [STOCK, 200], ...answers };
  const fetchMock = vi.fn(async (url) => {
    const found = routes[String(url)];
    return found ? reply(found[0], found[1]) : reply({ message: "not found" }, 404);
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** A fetch that waits until the test answers it: waiting[url](reply(...)). */
export function heldFetch() {
  const waiting = {};
  const fetchMock = vi.fn(
    (url) =>
      new Promise((resolve) => {
        waiting[String(url)] = resolve;
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return { fetchMock, waiting };
}
