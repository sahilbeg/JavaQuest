// The price list. Other modules ask this one for prices.

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };

// TODO 1: add a named export called DELIVERY_PAISE, set to 6000 (₹60)

export function priceOf(code) {
  // TODO 2: return the price of code from PRICES, or 0 when the code isn't on the list
  return 0;
}
