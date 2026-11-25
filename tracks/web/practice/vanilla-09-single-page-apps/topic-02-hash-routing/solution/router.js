// A tiny router for dhaaga.in that reads the part of the URL after the # sign.
// A route looks like { path: "/items/:id", view: (params) => element }.

/** "#/items/DC-1042" gives "/items/DC-1042". An empty hash, or just "#", gives "/". */
export function pathFromHash(hash) {
  const path = hash.slice(1);
  return path === "" ? "/" : path;
}

/**
 * Compares one pattern, such as "/items/:id", with one path, such as "/items/DC-1042".
 * Returns the params, such as { id: "DC-1042" }, or null when they don't match.
 */
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

/**
 * Draws the screen for the current hash into outlet now, and again on every hashchange.
 * notFound(path) gives the screen for an address no route matches. Returns a function that stops the router.
 */
export function startHashRouter(routes, outlet, notFound) {
  function render() {
    const path = pathFromHash(location.hash);
    const match = matchRoute(routes, path);
    outlet.replaceChildren(match ? match.route.view(match.params) : notFound(path));
  }

  render();
  window.addEventListener("hashchange", render);
  return () => window.removeEventListener("hashchange", render);
}
