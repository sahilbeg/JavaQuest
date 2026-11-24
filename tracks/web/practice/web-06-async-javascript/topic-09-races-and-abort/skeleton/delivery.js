// The PIN box at checkout: GET /api/delivery?pin=411045 answers { deliverable: true, days: 2 } or { deliverable: false }.

/**
 * Returns check(pin), which asks the server about a PIN and calls show(text) with the answer:
 *   "Delivery to 411045 in 2 days"  or  "Sorry, we don't deliver to 560001 yet"
 * Only the newest check may call show. An answer to an older check is thrown away.
 */
export function createDeliveryChecker(show) {
  // TODO 1: keep a counter here, outside check, that remembers the newest check's number.

  return async function check(pin) {
    // TODO 2: add one to the counter and remember this check's own number.
    const response = await fetch(`/api/delivery?pin=${pin}`);
    const data = await response.json();
    // TODO 3: if a newer check has started since, return without calling show.
    show(data.deliverable ? `Delivery to ${pin} in ${data.days} days` : `Sorry, we don't deliver to ${pin} yet`);
  };
}

/**
 * Like fetch(url), but gives up after ms milliseconds: the request is aborted and the promise rejects
 * with an AbortError. When the answer comes in time, resolves with the response.
 */
export async function fetchWithTimeout(url, ms) {
  // TODO 4: make an AbortController, and start a timer that calls controller.abort() after ms.
  // TODO 5: fetch url with { signal: controller.signal } and return the response.
  // TODO 6: clear the timer in finally, so it never fires after the answer arrives.
  return null;
}
