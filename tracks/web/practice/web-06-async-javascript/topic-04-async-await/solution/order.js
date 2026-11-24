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
  try {
    await api.reserve(order.itemId);
  } catch (error) {
    return `Sorry, ${order.itemId} just sold out.`;
  }

  try {
    const receipt = await api.pay(order.paise);
    return `Order ${order.id} confirmed, UPI ref ${receipt.ref}`;
  } catch (error) {
    await api.release(order.itemId);
    return `Payment failed for ${order.id}. Please try again.`;
  }
}

/**
 * getStock(id) is an async function that gives the number in stock for one item.
 * Returns the total stock of all the ids, asking for one id at a time.
 */
export async function totalStock(getStock, ids) {
  let total = 0;
  for (const id of ids) {
    total = total + (await getStock(id));
  }
  return total;
}
