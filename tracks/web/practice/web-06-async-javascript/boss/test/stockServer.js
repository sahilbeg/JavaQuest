import { vi } from "vitest";

export const shelf = { "DC-1042": 0, "DC-2001": 12, "DC-3001": 3 };

/**
 * A pretend stock API on slow 3G, installed as the global fetch. Nothing answers until the test says so:
 *   answer(id)          -> 200 { id, stock }
 *   breakDown(id)       -> 500 { error }
 *   dropConnection(id)  -> fetch rejects with TypeError("Failed to fetch")
 * With honourAbort, a request whose signal aborts rejects at once with an AbortError, like the real fetch.
 */
export function slowServer({ honourAbort = false } = {}) {
  const waiting = new Map();
  const fetchMock = vi.fn(
    (url, options = {}) =>
      new Promise((resolve, reject) => {
        const id = String(url).split("/").pop();
        waiting.set(id, { resolve, reject });
        if (honourAbort) {
          options.signal?.addEventListener("abort", () => {
            const error = new Error("This operation was aborted");
            error.name = "AbortError";
            reject(error);
          });
        }
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return {
    fetchMock,
    answer(id) {
      waiting.get(id)?.resolve({ ok: true, status: 200, json: async () => ({ id, stock: shelf[id] }) });
    },
    breakDown(id) {
      waiting.get(id)?.resolve({ ok: false, status: 500, json: async () => ({ error: "Server error" }) });
    },
    dropConnection(id) {
      waiting.get(id)?.reject(new TypeError("Failed to fetch"));
    },
  };
}

/** Waits for show(...) to finish. Returns the error it threw, or null. */
export async function settle(promise) {
  try {
    await promise;
    return null;
  } catch (error) {
    return error;
  }
}

/** A fresh panel element for each test. */
export function freshPanel() {
  document.body.innerHTML = `<p id="stock"></p>`;
  return document.querySelector("#stock");
}
