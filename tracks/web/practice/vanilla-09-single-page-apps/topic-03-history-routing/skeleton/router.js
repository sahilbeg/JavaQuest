// A router for dhaaga.in with clean URLs like /items/DC-1042, using the History API.
import { matchRoute } from "./match.js";

/**
 * routes: [{ path, view }]. outlet: the element the screens go in. notFound(path): the screen for unknown paths.
 * Returns { start, stop, navigate }.
 */
export function createRouter(routes, outlet, notFound) {
  /** Draws the screen for the path in the address bar right now. */
  function render() {
    // TODO 1: match location.pathname with matchRoute. Put match.route.view(match.params) in the outlet
    //         with replaceChildren, or notFound(path) when nothing matches.
  }

  /** Moves to path: a new history entry, then the new screen. */
  function navigate(path) {
    // TODO 2: add a history entry for path with history.pushState(null, "", path), then call render().
  }

  /** Turns taps on the app's own links into navigate() calls. */
  function onClick(event) {
    // TODO 3: find the tapped link with event.target.closest("a[data-link]"). Do nothing when there is none,
    //         or when ctrlKey, metaKey, shiftKey or altKey is pressed. Otherwise call event.preventDefault()
    //         and navigate to the link's href (use link.getAttribute("href")).
  }

  function start() {
    // TODO 4: draw once now, then listen for "popstate" on window (with render)
    //         and for "click" on document (with onClick).
  }

  function stop() {
    // TODO 5: remove both listeners that start added.
  }

  return { start, stop, navigate };
}
