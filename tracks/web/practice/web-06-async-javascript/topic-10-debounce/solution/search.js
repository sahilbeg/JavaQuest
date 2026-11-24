// Search as you type on dhaaga.in. GET /api/catalogue?q=golu answers with the matching items,
// for example [{ "id": "DC-1042", "name": "Golu the elephant" }].

/**
 * Returns a new function that waits until calls have stopped for ms milliseconds,
 * then calls fn once, with the arguments of the last call.
 */
export function debounce(fn, ms) {
  let timer = null;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), ms);
  };
}

/**
 * Returns onType(text), to be called on every input event.
 * After a pause of ms milliseconds it searches once:
 *   - text is trimmed first. Empty text calls show([]) without asking the server.
 *   - otherwise it fetches /api/catalogue?q=<text> and calls show with the items' names, such as ["Golu the elephant"].
 *   - only the newest search may call show. An answer to an older search is ignored.
 */
export function createSearch(show, ms = 300) {
  let latest = 0;
  return debounce(async (text) => {
    latest = latest + 1;
    const mine = latest;
    const q = text.trim();
    if (q === "") {
      show([]);
      return;
    }
    const response = await fetch(`/api/catalogue?${new URLSearchParams({ q })}`);
    const items = await response.json();
    if (mine !== latest) return;
    show(items.map((item) => item.name));
  }, ms);
}
