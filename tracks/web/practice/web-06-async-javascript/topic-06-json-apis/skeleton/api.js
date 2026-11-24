// Checkout's two calls to Dhaaga's API: the PIN check and placing the order.

/** The delivery endpoint for a PIN, for example "/api/delivery?pin=411001". */
export function deliveryUrl(pin) {
  // TODO 1: build the URL with a query string. Use new URLSearchParams({ pin }).
  return "";
}

/**
 * Asks GET /api/delivery?pin=... and returns the parsed body,
 * for example { deliverable: true, days: 2 } or { deliverable: false }.
 * Throws an Error that includes the status when the answer isn't ok.
 */
export async function checkDelivery(pin) {
  // TODO 2: fetch deliveryUrl(pin). If response.ok is false, throw an Error with the status in its message.
  // TODO 3: return the JSON body.
  return null;
}

/**
 * The text under the PIN box:
 *   deliverable  -> "Delivery to 411001 in 2 days"
 *   otherwise    -> "Sorry, we don't deliver to 560001 yet"
 */
export function deliveryText(pin, result) {
  // TODO 4: return the right sentence, using the pin and result.days.
  return "";
}

/**
 * POSTs the order to /api/orders as JSON and returns the new order's id, such as "order-17".
 * When the answer isn't ok, throws an Error whose message is the server's own error text.
 */
export async function createOrder(order) {
  // TODO 5: fetch "/api/orders" with method "POST", the header "Content-Type": "application/json",
  //         and the order as JSON text in body.
  // TODO 6: read the JSON body. If response.ok is false, throw new Error(data.error). Otherwise return data.id.
  return "";
}
