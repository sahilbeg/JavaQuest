// Checkout's two calls to Dhaaga's API: the PIN check and placing the order.

/** The delivery endpoint for a PIN, for example "/api/delivery?pin=411001". */
export function deliveryUrl(pin) {
  return `/api/delivery?${new URLSearchParams({ pin })}`;
}

/**
 * Asks GET /api/delivery?pin=... and returns the parsed body,
 * for example { deliverable: true, days: 2 } or { deliverable: false }.
 * Throws an Error that includes the status when the answer isn't ok.
 */
export async function checkDelivery(pin) {
  const response = await fetch(deliveryUrl(pin));
  if (!response.ok) {
    throw new Error(`Delivery check failed: ${response.status}`);
  }
  return response.json();
}

/**
 * The text under the PIN box:
 *   deliverable  -> "Delivery to 411001 in 2 days"
 *   otherwise    -> "Sorry, we don't deliver to 560001 yet"
 */
export function deliveryText(pin, result) {
  return result.deliverable ? `Delivery to ${pin} in ${result.days} days` : `Sorry, we don't deliver to ${pin} yet`;
}

/**
 * POSTs the order to /api/orders as JSON and returns the new order's id, such as "order-17".
 * When the answer isn't ok, throws an Error whose message is the server's own error text.
 */
export async function createOrder(order) {
  const response = await fetch("/api/orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error);
  }
  return data.id;
}
