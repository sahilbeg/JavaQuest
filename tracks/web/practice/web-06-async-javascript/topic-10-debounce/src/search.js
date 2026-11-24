// Search as you type on dhaaga.in. GET /api/catalogue?q=golu answers with the matching items,
// for example [{ "id": "DC-1042", "name": "Golu the elephant" }].

/**
 * Returns a new function that waits until calls have stopped for ms milliseconds,
 * then calls fn once, with the arguments of the last call.
 */
export function debounce(fn, ms) {
  // TODO 1: keep a timer variable here, outside the function you return.
  // TODO 2: return (...args) => { ... } that cancels the waiting timer with clearTimeout,
  //         then starts a new one with setTimeout that calls fn(...args) after ms.
  return () => {};
}

/**
 * Returns onType(text), to be called on every input event.
 * After a pause of ms milliseconds it searches once:
 *   - text is trimmed first. Empty text calls show([]) without asking the server.
 *   - otherwise it fetches /api/catalogue?q=<text> and calls show with the items' names, such as ["Golu the elephant"].
 *   - only the newest search may call show. An answer to an older search is ignored.
 */
export function createSearch(show, ms = 300) {
  // TODO 3: keep a counter for the newest search, as you did for the PIN checker.
  // TODO 4: return debounce(async (text) => { ... }, ms). Inside, count this search and trim the text.
  // TODO 5: for empty text, call show([]) and stop. Otherwise fetch `/api/catalogue?${new URLSearchParams({ q })}`.
  // TODO 6: if this is still the newest search, call show with the names of the items.
  return () => {};
}
