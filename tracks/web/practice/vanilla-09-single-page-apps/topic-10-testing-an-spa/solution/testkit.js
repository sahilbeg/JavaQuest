// The team's kit for testing the single-page app in jsdom, like a tiny Testing Library.

/** Puts the app at path, the way Back or a deep link would: the address changes, then popstate fires. */
export function visit(path) {
  history.replaceState(null, "", path);
  window.dispatchEvent(new PopStateEvent("popstate"));
}

/**
 * A pretend server. answers maps paths to JSON bodies, such as { "/api/items": [...] }.
 * Returns { fetch, calls }: fetch answers a known path with 200 and its body, and any other path with
 * 404 and { error: "Not found" }. calls lists every path asked for, in order.
 */
export function fakeServer(answers) {
  const calls = [];
  async function fetch(path) {
    calls.push(path);
    if (Object.hasOwn(answers, path)) {
      return { ok: true, status: 200, json: async () => answers[path] };
    }
    return { ok: false, status: 404, json: async () => ({ error: "Not found" }) };
  }
  return { fetch, calls };
}

/**
 * Calls check() now and then every 10 ms, until it gives a truthy value, and resolves with that value.
 * Rejects with Error("waitFor timed out after <timeout> ms") once timeout ms have passed.
 */
export function waitFor(check, timeout = 1000) {
  return new Promise((resolve, reject) => {
    const started = Date.now();
    function attempt() {
      const value = check();
      if (value) {
        resolve(value);
      } else if (Date.now() - started >= timeout) {
        reject(new Error(`waitFor timed out after ${timeout} ms`));
      } else {
        setTimeout(attempt, 10);
      }
    }
    attempt();
  });
}

/** The text of an element as a person reads it: every run of spaces and new lines becomes one space, ends trimmed. */
export function textOf(element) {
  return element.textContent.replace(/\s+/g, " ").trim();
}

/** Clears what a test left behind: the page's body, localStorage, and the address (back to "/"). */
export function reset() {
  document.body.replaceChildren();
  localStorage.clear();
  history.replaceState(null, "", "/");
}
