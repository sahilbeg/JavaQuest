// Route matching from the hash-routing ticket. This file is finished.
// A route looks like { path: "/items/:id", view: (params) => element }.

/** The params for path, such as { id: "DC-1042" }, or null when pattern doesn't match it. */
export function matchOne(pattern, path) {
  const wanted = pattern.split("/");
  const parts = path.split("/");
  if (wanted.length !== parts.length) {
    return null;
  }
  const params = {};
  for (let i = 0; i < wanted.length; i++) {
    if (wanted[i].startsWith(":") && parts[i] !== "") {
      params[wanted[i].slice(1)] = parts[i];
    } else if (wanted[i] !== parts[i]) {
      return null;
    }
  }
  return params;
}

/** The first route whose pattern matches path, as { route, params }, or null when none does. */
export function matchRoute(routes, path) {
  for (const route of routes) {
    const params = matchOne(route.path, path);
    if (params !== null) {
      return { route, params };
    }
  }
  return null;
}
