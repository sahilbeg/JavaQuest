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
    if (promise === null) {
      promise = loader().catch((error) => {
        promise = null;
        throw error;
      });
    }
    return promise;
  };
}

/** Shows LOADING, waits for the module, then draws module.default(params) in the outlet. Shows FAILED on failure. Never throws. */
export async function showLazyView(outlet, load, params) {
  outlet.textContent = LOADING;
  try {
    const module = await load();
    outlet.replaceChildren(module.default(params));
  } catch {
    outlet.textContent = FAILED;
  }
}
