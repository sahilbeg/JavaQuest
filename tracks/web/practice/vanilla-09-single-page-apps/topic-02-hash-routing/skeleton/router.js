// A tiny router for dhaaga.in that reads the part of the URL after the # sign.
// A route looks like { path: "/items/:id", view: (params) => element }.

/** "#/items/DC-1042" gives "/items/DC-1042". An empty hash, or just "#", gives "/". */
export function pathFromHash(hash) {
  // TODO 1: drop the first character (the #). If nothing is left, return "/".
  return "";
}

/**
 * Compares one pattern, such as "/items/:id", with one path, such as "/items/DC-1042".
 * Returns the params, such as { id: "DC-1042" }, or null when they don't match.
 */
export function matchOne(pattern, path) {
  const wanted = pattern.split("/");
  const parts = path.split("/");
  // TODO 2: return null when the two lists have different lengths. Then compare them part by part:
  //         a pattern part starting with ":" saves a non-empty path part in params (":id" saves params.id),
  //         and any other part must be exactly equal, or the answer is null.
  return null;
}

/** The first route whose pattern matches path, as { route, params }, or null when none does. */
export function matchRoute(routes, path) {
  // TODO 3: try the routes in order with matchOne, and return { route, params } for the first one that matches.
  return null;
}

/**
 * Draws the screen for the current hash into outlet now, and again on every hashchange.
 * notFound(path) gives the screen for an address no route matches. Returns a function that stops the router.
 */
export function startHashRouter(routes, outlet, notFound) {
  function render() {
    // TODO 4: get the path from location.hash and match it. Put match.route.view(match.params) in the outlet
    //         with outlet.replaceChildren(...). With no match, put notFound(path) there instead.
  }

  // TODO 5: call render() once now, listen for "hashchange" on window with render,
  //         and return a function that removes that listener.
  return () => {};
}
