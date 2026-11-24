// Placing an order on dhaaga.in, one step after another.

/**
 * api has three async functions:
 *   api.reserve(itemId) holds the item for this customer. It throws if the item just sold out.
 *   api.pay(paise)      takes the UPI payment and gives back a receipt { ref }. It throws if the bank declines.
 *   api.release(itemId) gives a held item back, so someone else can buy it.
 * order looks like { id: "order-17", itemId: "DC-1042", paise: 45000 }.
 *
 * Returns one of:
 *   "Order order-17 confirmed, UPI ref UPI-5521"
 *   "Sorry, DC-1042 just sold out."              (reserving failed: don't take any money)
 *   "Payment failed for order-17. Please try again."  (paying failed: release the item first)
 */
export async function placeOrder(api, order) {
  // TODO 1: wait for api.reserve(order.itemId). If it throws, return the sold-out message and stop.
  api.reserve(order.itemId);

  // TODO 2: wait for api.pay(order.paise), so receipt is the real receipt and not a promise.
  const receipt = api.pay(order.paise);

  // TODO 3: if paying throws, wait for api.release(order.itemId), then return the payment-failed message.
  return `Order ${order.id} confirmed, UPI ref ${receipt.ref}`;
}

/**
 * getStock(id) is an async function that gives the number in stock for one item.
 * Returns the total stock of all the ids, asking for one id at a time.
 */
export async function totalStock(getStock, ids) {
  // TODO 4: loop over ids with for...of, await getStock(id) for each, and add it to a running total.
  return 0;
}
