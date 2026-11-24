// Builds the lines of a receipt from the other two modules.
import rupees from "./format.js";
import * as prices from "./prices.js";

export const FREE_DELIVERY_ABOVE_PAISE = 99900;

export function receiptLine(code, qty) {
  // TODO 5: return qty + " x " + code + " = " + the line's price in rupees (use prices.priceOf and rupees)
  return "";
}

export function deliveryLine(subtotalPaise) {
  // TODO 6: return "Delivery: free" above FREE_DELIVERY_ABOVE_PAISE, otherwise "Delivery: " + rupees(prices.DELIVERY_PAISE)
  return "";
}
