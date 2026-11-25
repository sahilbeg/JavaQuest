import { vi } from "vitest";

/** A pretend fetch response: ok for 2xx codes, with the data as its JSON. */
export function reply(data: unknown, status = 200) {
  return { ok: status >= 200 && status < 300, status, json: async () => data };
}

/** Replaces the global fetch with a mock that answers with handler(url). Returns the mock. */
export function stubFetch(handler: (url: string) => unknown) {
  const fetchMock = vi.fn(handler);
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** A fetch whose answers wait until the test releases them: waiting[url](response). */
export function heldFetch() {
  const waiting: Record<string, (response: unknown) => void> = {};
  const fetchMock = stubFetch((url) => new Promise((resolve) => (waiting[url] = resolve)));
  return { fetchMock, waiting };
}

/** Gives promises a moment to finish, so a late update would show up before we check. */
export function settle() {
  return new Promise((resolve) => setTimeout(resolve, 20));
}
