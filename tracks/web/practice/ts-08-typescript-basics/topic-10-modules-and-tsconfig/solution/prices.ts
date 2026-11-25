// Dhaaga's price list. Every amount is whole paise: ₹1 is 100 paise.
import type { ItemCode } from "./types.ts";

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };

export function priceOf(code: ItemCode): number {
  return PRICES[code];
}
