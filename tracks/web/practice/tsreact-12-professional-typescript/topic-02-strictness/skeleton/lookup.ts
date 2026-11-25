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
  // TODO 1: return true only when text is one of PRICES' own keys. The in operator also finds keys every object has, like toString.
  return false;
}

/** The price of a code a person typed, or null when there is no such item. */
export function priceOf(typed: string): number | null {
  // TODO 2: trim the text and make it upper case. If isItemCode says yes, return its price from PRICES; otherwise return null.
  return null;
}

/** The line with the lowest price for one item, or null for an empty cart. */
export function cheapestLine(lines: readonly CartLine[]): CartLine | null {
  // TODO 3: keep the cheapest line so far (CartLine | null, starting at null) and return it. On a tie, keep the first. Don't reorder lines.
  return null;
}

/** Words for a customer, whatever was thrown. */
export function describeError(error: unknown): string {
  // TODO 4: an Error gives its message, a string gives itself, and anything else gives "Something went wrong."
  return "";
}
