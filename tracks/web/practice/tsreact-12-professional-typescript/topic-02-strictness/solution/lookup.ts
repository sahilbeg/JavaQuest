/** The five things Dhaaga sells, by code. */
export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

/** Prices in paise. */
export const PRICES: Record<ItemCode, number> = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };

export interface CartLine {
  code: ItemCode;
  qty: number;
}

/** True only for the five real codes, written exactly. */
export function isItemCode(text: string): text is ItemCode {
  return Object.hasOwn(PRICES, text);
}

/** The price of a code a person typed, or null when there is no such item. */
export function priceOf(typed: string): number | null {
  const code = typed.trim().toUpperCase();
  return isItemCode(code) ? PRICES[code] : null;
}

/** The line with the lowest price for one item, or null for an empty cart. */
export function cheapestLine(lines: readonly CartLine[]): CartLine | null {
  let cheapest: CartLine | null = null;
  for (const line of lines) {
    if (cheapest === null || PRICES[line.code] < PRICES[cheapest.code]) {
      cheapest = line;
    }
  }
  return cheapest;
}

/** Words for a customer, whatever was thrown. */
export function describeError(error: unknown): string {
  if (error instanceof Error) return error.message;
  if (typeof error === "string") return error;
  return "Something went wrong.";
}
