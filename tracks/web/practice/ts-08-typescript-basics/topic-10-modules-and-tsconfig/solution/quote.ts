// A quote for a WhatsApp customer, built from the other modules.
import type { CartLine } from "./types.ts";
import { priceOf } from "./prices.ts";
import { formatRupees } from "./money.ts";

export function quoteLine(line: CartLine): string {
  return `${line.code} x ${line.qty}: ${formatRupees(priceOf(line.code) * line.qty)}`;
}

export function quoteTotal(lines: CartLine[]): number {
  let total = 0;
  for (const line of lines) {
    total += priceOf(line.code) * line.qty;
  }
  return total;
}
