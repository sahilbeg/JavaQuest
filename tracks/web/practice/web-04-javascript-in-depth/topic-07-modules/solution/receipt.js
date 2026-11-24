// Builds the lines of a receipt from the other two modules.
import rupees from "./format.js";
import * as prices from "./prices.js";

export const FREE_DELIVERY_ABOVE_PAISE = 99900;

export function receiptLine(code, qty) {
  return qty + " x " + code + " = " + rupees(prices.priceOf(code) * qty);
}

export function deliveryLine(subtotalPaise) {
  if (subtotalPaise > FREE_DELIVERY_ABOVE_PAISE) {
    return "Delivery: free";
  }
  return "Delivery: " + rupees(prices.DELIVERY_PAISE);
}
