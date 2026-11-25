/** A pretend cookie store: get(name) gives { name, value } or undefined, like the real one. */
export function fakeCookies(values = {}) {
  return {
    get: (name) => (Object.hasOwn(values, name) ? { name, value: values[name] } : undefined),
    has: (name) => Object.hasOwn(values, name),
  };
}

/** A pretend NextRequest for a path on http://localhost, with these cookies. */
export function fakeRequest(path, cookies = {}) {
  const url = "http://localhost" + path;
  return { url, nextUrl: new URL(url), method: "GET", cookies: fakeCookies(cookies) };
}
