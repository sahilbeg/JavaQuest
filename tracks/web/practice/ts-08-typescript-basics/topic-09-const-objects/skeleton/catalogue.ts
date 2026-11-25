// Dhaaga's fixed choices, written once with as const. Every amount is whole paise: ₹1 is 100 paise.

export const ITEM_CODES = ["CST", "KEY", "AMI", "BAG", "BTY"] as const;
export type ItemCode = (typeof ITEM_CODES)[number];

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 } as const;

export const PAYMENT_MODES = ["CASH", "UPI", "CARD"] as const;
export type PaymentMode = (typeof PAYMENT_MODES)[number];

export function parseCode(text: string): ItemCode | null {
  // TODO 1: trim and upper-case the text. Return the matching code from ITEM_CODES, or null when none matches.
  return null;
}

export function priceOf(code: ItemCode): number {
  // TODO 2: the code's price from PRICES
  return 0;
}

export function parseMode(text: string): PaymentMode | null {
  // TODO 3: like parseCode, but for PAYMENT_MODES
  return null;
}

export function priceBoard(): string[] {
  // TODO 4: one line per code, in ITEM_CODES order: "CST ₹150.00", "KEY ₹120.00", ...
  return [];
}

export function codesUpTo(maxPaise: number): ItemCode[] {
  // TODO 5: the codes whose price is at most maxPaise, in ITEM_CODES order
  return [];
}
