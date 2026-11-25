// dhaaga.in's router. A route looks like:
// { path: "/items/:id", title: (params) => "Golu the elephant", view: (params, query) => element }
// query is a URLSearchParams for the part after ?, such as ?colour=Indigo.
import { matchRoute } from "./match.js";

export const SITE = "Dhaaga & Co.";

/** Old addresses printed on flyers and price tags, and where they live now. */
export const REDIRECTS = { "/shop": "/items", "/golu": "/items/DC-1042" };

export function createRouter(routes, outlet, notFound) {
  /** The address bar's path and query, such as "/items?colour=Indigo". */
  function here() {
    return location.pathname + location.search;
  }

  /** Draws the screen and sets the title for whatever the address bar says right now. */
  function show() {
    const match = matchRoute(routes, location.pathname);
    const query = new URLSearchParams(location.search);
    if (match) {
      outlet.replaceChildren(match.route.view(match.params, query));
      document.title = `${match.route.title(match.params)} | ${SITE}`;
    } else {
      outlet.replaceChildren(notFound(location.pathname));
      document.title = `Page not found | ${SITE}`;
    }
  }

  function navigate(path) {
    const target = REDIRECTS[path] ?? path;
    if (target !== here()) {
      history.pushState(null, "", target);
    }
    show();
  }

  function start() {
    const target = REDIRECTS[location.pathname];
    if (target !== undefined) {
      history.replaceState(null, "", target);
    }
    show();
    window.addEventListener("popstate", show);
  }

  function stop() {
    window.removeEventListener("popstate", show);
  }

  return { start, stop, navigate };
}
