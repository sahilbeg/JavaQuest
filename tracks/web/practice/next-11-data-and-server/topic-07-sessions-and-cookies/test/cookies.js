/**
 * A pretend cookie store with the same methods as the one from `await cookies()` in Next.js.
 * Pass { name: value } to start with some cookies. `setCalls` records every set, with its options.
 */
export function fakeCookies(initial = {}) {
  const jar = new Map(Object.entries(initial).map(([name, value]) => [name, { name, value }]));
  const setCalls = [];
  return {
    get: (name) => jar.get(name),
    has: (name) => jar.has(name),
    set: (name, value, options = {}) => {
      jar.set(name, { name, value });
      setCalls.push({ name, value, options });
    },
    delete: (name) => {
      jar.delete(name);
    },
    setCalls,
  };
}
