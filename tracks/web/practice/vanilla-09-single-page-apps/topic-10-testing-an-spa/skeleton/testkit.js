// The team's kit for testing the single-page app in jsdom, like a tiny Testing Library.

/** Puts the app at path, the way Back or a deep link would: the address changes, then popstate fires. */
export function visit(path) {
  // TODO 1: change the address with history.replaceState(null, "", path),
  //         then fire popstate on window with window.dispatchEvent(new PopStateEvent("popstate")).
}

/**
 * A pretend server. answers maps paths to JSON bodies, such as { "/api/items": [...] }.
 * Returns { fetch, calls }: fetch answers a known path with 200 and its body, and any other path with
 * 404 and { error: "Not found" }. calls lists every path asked for, in order.
 */
export function fakeServer(answers) {
  const calls = [];
  async function fetch(path) {
    // TODO 2: push path onto calls. For a path in answers (Object.hasOwn(answers, path)), return
    //         { ok: true, status: 200, json: async () => answers[path] }. Otherwise return ok false, status 404,
    //         and a json() that gives { error: "Not found" }.
    return { ok: false, status: 0, json: async () => null };
  }
  return { fetch, calls };
}

/**
 * Calls check() now and then every 10 ms, until it gives a truthy value, and resolves with that value.
 * Rejects with Error("waitFor timed out after <timeout> ms") once timeout ms have passed.
 */
export function waitFor(check, timeout = 1000) {
  // TODO 3: return a new Promise. Inside, remember Date.now(), and write an attempt() function: if check() gives
  //         a truthy value, resolve with it; else if timeout ms have passed, reject with the Error above;
  //         else setTimeout(attempt, 10). Call attempt() once to start.
  return Promise.resolve(null);
}

/** The text of an element as a person reads it: every run of spaces and new lines becomes one space, ends trimmed. */
export function textOf(element) {
  // TODO 4: take element.textContent, replace every run of whitespace (/\s+/g) with one space, and trim it.
  return "";
}

/** Clears what a test left behind: the page's body, localStorage, and the address (back to "/"). */
export function reset() {
  // TODO 5: empty document.body with replaceChildren(), clear localStorage, and replaceState back to "/".
}
