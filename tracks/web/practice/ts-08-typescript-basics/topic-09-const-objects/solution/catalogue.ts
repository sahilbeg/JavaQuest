// Dhaaga's fixed choices, written once with as const. Every amount is whole paise: ₹1 is 100 paise.

export const ITEM_CODES = ["CST", "KEY", "AMI", "BAG", "BTY"] as const;
export type ItemCode = (typeof ITEM_CODES)[number];

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 } as const;

export const PAYMENT_MODES = ["CASH", "UPI", "CARD"] as const;
export type PaymentMode = (typeof PAYMENT_MODES)[number];

export function parseCode(text: string): ItemCode | null {
  const cleaned = text.trim().toUpperCase();
  return ITEM_CODES.find((code) => code === cleaned) ?? null;
}

export function priceOf(code: ItemCode): number {
  return PRICES[code];
}

export function parseMode(text: string): PaymentMode | null {
  const cleaned = text.trim().toUpperCase();
  return PAYMENT_MODES.find((mode) => mode === cleaned) ?? null;
}

export function priceBoard(): string[] {
  return ITEM_CODES.map((code) => `${code} ₹${(PRICES[code] / 100).toFixed(2)}`);
}

export function codesUpTo(maxPaise: number): ItemCode[] {
  return ITEM_CODES.filter((code) => PRICES[code] <= maxPaise);
}
