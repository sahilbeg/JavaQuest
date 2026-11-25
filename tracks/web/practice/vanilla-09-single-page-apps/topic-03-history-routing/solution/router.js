// A router for dhaaga.in with clean URLs like /items/DC-1042, using the History API.
import { matchRoute } from "./match.js";

/**
 * routes: [{ path, view }]. outlet: the element the screens go in. notFound(path): the screen for unknown paths.
 * Returns { start, stop, navigate }.
 */
export function createRouter(routes, outlet, notFound) {
  /** Draws the screen for the path in the address bar right now. */
  function render() {
    const path = location.pathname;
    const match = matchRoute(routes, path);
    outlet.replaceChildren(match ? match.route.view(match.params) : notFound(path));
  }

  /** Moves to path: a new history entry, then the new screen. */
  function navigate(path) {
    history.pushState(null, "", path);
    render();
  }

  /** Turns taps on the app's own links into navigate() calls. */
  function onClick(event) {
    const link = event.target.closest("a[data-link]");
    if (link === null || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
      return;
    }
    event.preventDefault();
    navigate(link.getAttribute("href"));
  }

  function start() {
    render();
    window.addEventListener("popstate", render);
    document.addEventListener("click", onClick);
  }

  function stop() {
    window.removeEventListener("popstate", render);
    document.removeEventListener("click", onClick);
  }

  return { start, stop, navigate };
}
