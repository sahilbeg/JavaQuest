import { vi } from "vitest";

export const shelf = { "DC-1042": 3, "DC-2001": 12, "DC-3001": 0 };

/**
 * A pretend stock API on slow 3G, installed as the global fetch. Use it with vi.useFakeTimers().
 * Each answer arrives after delays[id] milliseconds (1000 if not given). Unknown ids get a 404.
 */
export function slowServer(delays = {}) {
  const fetchMock = vi.fn(
    (url) =>
      new Promise((resolve) => {
        const id = String(url).split("/").pop();
        setTimeout(() => {
          if (id in shelf) {
            resolve({ ok: true, status: 200, json: async () => ({ id, stock: shelf[id] }) });
          } else {
            resolve({ ok: false, status: 404, json: async () => ({ error: "No such item" }) });
          }
        }, delays[id] ?? 1000);
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** Watches a promise, so a test can check whether it has finished yet. */
export function track(promise) {
  const state = { done: false, value: undefined, error: null };
  promise.then(
    (value) => {
      state.done = true;
      state.value = value;
    },
    (error) => {
      state.done = true;
      state.error = error;
    },
  );
  return state;
}
