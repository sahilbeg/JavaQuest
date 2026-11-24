// The PIN box at checkout: GET /api/delivery?pin=411045 answers { deliverable: true, days: 2 } or { deliverable: false }.

/**
 * Returns check(pin), which asks the server about a PIN and calls show(text) with the answer:
 *   "Delivery to 411045 in 2 days"  or  "Sorry, we don't deliver to 560001 yet"
 * Only the newest check may call show. An answer to an older check is thrown away.
 */
export function createDeliveryChecker(show) {
  let latest = 0;

  return async function check(pin) {
    latest = latest + 1;
    const mine = latest;
    const response = await fetch(`/api/delivery?pin=${pin}`);
    const data = await response.json();
    if (mine !== latest) return;
    show(data.deliverable ? `Delivery to ${pin} in ${data.days} days` : `Sorry, we don't deliver to ${pin} yet`);
  };
}

/**
 * Like fetch(url), but gives up after ms milliseconds: the request is aborted and the promise rejects
 * with an AbortError. When the answer comes in time, resolves with the response.
 */
export async function fetchWithTimeout(url, ms) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), ms);
  try {
    return await fetch(url, { signal: controller.signal });
  } finally {
    clearTimeout(timer);
  }
}
