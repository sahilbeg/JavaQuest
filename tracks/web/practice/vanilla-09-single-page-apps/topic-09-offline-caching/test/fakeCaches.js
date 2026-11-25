// Pretend versions of the browser's Cache API, which jsdom doesn't have.
import { vi } from "vitest";

/** A response with a body, a status and clone(), like the real Response. */
export function fakeResponse(body, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    body,
    clone() {
      return fakeResponse(body, status);
    },
  };
}

/** One named cache: saved responses by path. entries: { "/main.js": response }. */
export function fakeCache(entries = {}) {
  const saved = new Map(Object.entries(entries));
  return {
    saved,
    match: vi.fn(async (path) => saved.get(path)),
    put: vi.fn(async (path, response) => {
      saved.set(path, response);
    }),
    addAll: vi.fn(async (paths) => {
      for (const path of paths) {
        saved.set(path, fakeResponse(`file ${path}`));
      }
    }),
  };
}

/** The whole caches object, holding caches by name. */
export function fakeCaches(names = []) {
  const all = new Map(names.map((name) => [name, fakeCache()]));
  return {
    all,
    open: vi.fn(async (name) => {
      if (!all.has(name)) {
        all.set(name, fakeCache());
      }
      return all.get(name);
    }),
    keys: vi.fn(async () => [...all.keys()]),
    delete: vi.fn(async (name) => all.delete(name)),
  };
}
