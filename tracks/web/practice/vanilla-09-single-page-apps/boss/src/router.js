// dhaaga.in's router. A route looks like:
// { path: "/items/:id", title: (params) => "Golu the elephant", view: (params, query) => element }
// query is a URLSearchParams for the part after ?, such as ?colour=Indigo.
//
// INC-09: customers say the Back button shows the wrong page. Read the README for the three tiers.
// TODO 1 (bronze): Back and Forward must draw the page the address bar now shows.
// TODO 2 (silver): tapping the page you are on adds no history entry, and old addresses in REDIRECTS
//                  leave exactly one entry, for the new address (on navigate and when the app starts).
// TODO 3 (gold): the title follows the screen everywhere, and ?colour= filters survive Back and Forward.
import { matchRoute } from "./match.js";

export const SITE = "Dhaaga & Co.";

/** Old addresses printed on flyers and price tags, and where they live now. */
export const REDIRECTS = { "/shop": "/items", "/golu": "/items/DC-1042" };

export function createRouter(routes, outlet, notFound) {
  let currentPath = "/";

  function show(path) {
    const url = new URL(path, "https://dhaaga.in");
    const match = matchRoute(routes, url.pathname);
    if (match) {
      outlet.replaceChildren(match.route.view(match.params, url.searchParams));
    } else {
      outlet.replaceChildren(notFound(url.pathname));
    }
  }

  function navigate(path) {
    history.pushState(null, "", path);
    const target = REDIRECTS[path];
    if (target !== undefined) {
      history.pushState(null, "", target);
      path = target;
    }
    currentPath = path;
    const match = matchRoute(routes, new URL(path, "https://dhaaga.in").pathname);
    document.title = match ? `${match.route.title(match.params)} | ${SITE}` : `Page not found | ${SITE}`;
    show(path);
  }

  function onPopState() {
    show(currentPath);
  }

  function start() {
    currentPath = location.pathname + location.search;
    show(currentPath);
    window.addEventListener("popstate", onPopState);
  }

  function stop() {
    window.removeEventListener("popstate", onPopState);
  }

  return { start, stop, navigate };
}
