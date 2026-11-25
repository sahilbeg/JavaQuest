// Loads a screen's code only when someone opens it, and shows a message while it arrives.

export const LOADING = "Loading this page...";
export const FAILED = "Couldn't load this page. Check your connection and try again.";

/**
 * Wraps a loader such as () => import("./checkout.js") so it runs at most once.
 * Returns load(): every call gets the same promise, until a failure, after which the next call tries again.
 */
export function lazy(loader) {
  let promise = null;
  return function load() {
    // TODO 1: the first time, call loader() and keep its promise in promise. Every later call returns that same promise.
    // TODO 2: if the promise fails, set promise back to null (so the next call tries again) and throw the error on:
    //         promise = loader().catch((error) => { promise = null; throw error; });
    return Promise.resolve(null);
  };
}

/** Shows LOADING, waits for the module, then draws module.default(params) in the outlet. Shows FAILED on failure. Never throws. */
export async function showLazyView(outlet, load, params) {
  // TODO 3: put LOADING in outlet.textContent, then await load() and draw module.default(params)
  //         in the outlet with replaceChildren.
  // TODO 4: wrap the waiting in try...catch, and put FAILED in outlet.textContent when loading fails.
}
