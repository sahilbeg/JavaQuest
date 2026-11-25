// A quote for a WhatsApp customer, built from the other modules.
import type { CartLine } from "./types.ts";
import { priceOf } from "./prices.ts";
import { formatRupees } from "./money.ts";

export function quoteLine(line: CartLine): string {
  // TODO 3: "BAG x 2: ₹1,798.00": the code, " x ", the quantity, ": " and the line's cost, using priceOf and formatRupees
  return "";
}

export function quoteTotal(lines: CartLine[]): number {
  // TODO 4: the total in paise of every line: price times quantity
  return 0;
}
